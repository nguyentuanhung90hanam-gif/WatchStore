package com.watchstore;

import com.watchstore.config.DBContext;
import com.watchstore.model.Category;
import com.watchstore.repository.CategoryRepository;
import com.watchstore.repository.CategoryRepositoryImpl;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CategoryMigrationVerificationTest {

    private static CategoryRepository categoryRepository;

    @BeforeAll
    public static void setUp() {
        categoryRepository = new CategoryRepositoryImpl();

        // Perform migration on live database if not already done
        try (Connection conn = DBContext.getConnection();
             Statement st = conn.createStatement()) {
            
            // Drop FK if exists
            st.execute("""
                IF EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_Categories_Parent')
                BEGIN
                    ALTER TABLE dbo.Categories DROP CONSTRAINT FK_Categories_Parent;
                END
                """);

            // Drop ParentCategoryID column if exists
            st.execute("""
                IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'Categories' AND COLUMN_NAME = 'ParentCategoryID')
                BEGIN
                    ALTER TABLE dbo.Categories DROP COLUMN ParentCategoryID;
                END
                """);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    @Order(1)
    public void testDatabaseSchemaAfterMigration() throws Exception {
        try (Connection conn = DBContext.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            
            // Check ParentCategoryID column is gone
            boolean parentCategoryColExists = false;
            try (ResultSet rs = meta.getColumns(null, "dbo", "Categories", "ParentCategoryID")) {
                if (rs.next()) {
                    parentCategoryColExists = true;
                }
            }
            assertFalse(parentCategoryColExists, "ParentCategoryID column must be dropped from dbo.Categories");

            // Check FK_Categories_Parent is gone
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM sys.foreign_keys WHERE name = 'FK_Categories_Parent'")) {
                assertTrue(rs.next());
                assertEquals(0, rs.getInt(1), "FK_Categories_Parent constraint must be dropped");
            }
        }
    }

    @Test
    @Order(2)
    public void testExistingCategoriesPreserved() {
        List<Category> categories = categoryRepository.findAll();
        assertNotNull(categories);
        assertFalse(categories.isEmpty(), "Categories must not be empty");

        boolean hasMen = categories.stream().anyMatch(c -> "MEN".equalsIgnoreCase(c.getCategoryCode()));
        boolean hasAutomatic = categories.stream().anyMatch(c -> "AUTOMATIC".equalsIgnoreCase(c.getCategoryCode()));
        boolean hasQuartz = categories.stream().anyMatch(c -> "QUARTZ".equalsIgnoreCase(c.getCategoryCode()));

        assertTrue(hasMen, "MEN category must exist");
        assertTrue(hasAutomatic, "AUTOMATIC category must exist");
        assertTrue(hasQuartz, "QUARTZ category must exist");
    }

    @Test
    @Order(3)
    public void testCategoryCrudOperations() {
        String testCode = "TEST-CAT-MIG";
        String testSlug = "dong-ho-test-migration";

        // Clean up pre-existing if any
        List<Category> searchBefore = categoryRepository.search(testCode);
        for (Category c : searchBefore) {
            if (testCode.equalsIgnoreCase(c.getCategoryCode())) {
                categoryRepository.delete(c.getCategoryId());
            }
        }

        // CREATE / SAVE
        Category newCat = new Category();
        newCat.setCategoryCode(testCode);
        newCat.setCategoryName("Đồng Hồ Test Migration");
        newCat.setSlug(testSlug);
        newCat.setDescription("Mô tả danh mục kiểm thử");
        newCat.setImageUrl("/assets/images/test.jpg");
        newCat.setDisplayOrder(99);
        newCat.setStatus("ACTIVE");

        categoryRepository.save(newCat);

        // READ / SEARCH
        List<Category> searchResults = categoryRepository.search("Test Migration");
        assertFalse(searchResults.isEmpty(), "Should find newly created category");
        Category found = searchResults.stream()
                .filter(c -> testCode.equalsIgnoreCase(c.getCategoryCode()))
                .findFirst()
                .orElse(null);

        assertNotNull(found, "Saved category must be found");
        assertEquals(testCode, found.getCategoryCode());
        assertEquals("Đồng Hồ Test Migration", found.getCategoryName());
        assertEquals(testSlug, found.getSlug());
        assertEquals("ACTIVE", found.getStatus());

        // UPDATE
        found.setCategoryName("Đồng Hồ Test Migration Đã Sửa");
        found.setStatus("INACTIVE");
        categoryRepository.update(found);

        Category updated = categoryRepository.findById(found.getCategoryId());
        assertNotNull(updated);
        assertEquals("Đồng Hồ Test Migration Đã Sửa", updated.getCategoryName());
        assertEquals("INACTIVE", updated.getStatus());

        // DUPLICATE CHECKS
        assertTrue(categoryRepository.existsByCode(testCode, null));
        assertFalse(categoryRepository.existsByCode(testCode, found.getCategoryId()));
        assertTrue(categoryRepository.existsBySlug(testSlug, null));
        assertFalse(categoryRepository.existsBySlug(testSlug, found.getCategoryId()));

        // DELETE
        assertFalse(categoryRepository.isCategoryInUse(found.getCategoryId()));
        categoryRepository.delete(found.getCategoryId());

        Category deleted = categoryRepository.findById(found.getCategoryId());
        assertNull(deleted, "Deleted category should no longer exist");
    }

    @Test
    @Order(4)
    public void testProductCategoryLink() throws Exception {
        try (Connection conn = DBContext.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("""
                 SELECT p.ProductID, p.ProductName, c.CategoryID, c.CategoryName
                 FROM dbo.Products p
                 INNER JOIN dbo.Categories c ON p.CategoryID = c.CategoryID
                 """)) {
            int count = 0;
            while (rs.next()) {
                count++;
                assertNotNull(rs.getString("ProductName"));
                assertNotNull(rs.getString("CategoryName"));
            }
            assertTrue(count > 0, "Products must link to Categories via CategoryID");
        }
    }
}
