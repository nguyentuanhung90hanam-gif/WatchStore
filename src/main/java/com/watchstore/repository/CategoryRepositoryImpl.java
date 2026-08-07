package com.watchstore.repository;
import com.watchstore.config.DBContext;
import com.watchstore.model.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryRepositoryImpl implements CategoryRepository {


    private Connection getConnection() throws SQLException {

        return DBContext.getConnection();

    }


    @Override
    public List<Category> findAll() {

        List<Category> list = new ArrayList<>();

        String sql = """
                SELECT *
                FROM Categories
                ORDER BY CategoryID DESC
                """;


        try(Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {


            while(rs.next()) {

                Category c = new Category();

                c.setCategoryId(rs.getInt("CategoryID"));

                if(rs.getObject("ParentID") != null){
                    c.setParentId(rs.getInt("ParentID"));
                }

                c.setCategoryCode(
                        rs.getString("CategoryCode")
                );

                c.setCategoryName(
                        rs.getString("CategoryName")
                );

                c.setSlug(
                        rs.getString("Slug")
                );

                c.setDescription(
                        rs.getString("Description")
                );

                c.setImage(
                        rs.getString("Image")
                );

                c.setSortOrder(
                        rs.getInt("SortOrder")
                );

                c.setStatus(
                        rs.getString("Status")
                );


                list.add(c);

            }


        } catch(Exception e){

            e.printStackTrace();

        }


        return list;
    }



    @Override
    public Category findById(Integer id) {

        String sql =
                "SELECT * FROM Categories WHERE CategoryID=?";


        try(Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {


            ps.setInt(1,id);

            ResultSet rs = ps.executeQuery();


            if(rs.next()) {

                Category c = new Category();

                c.setCategoryId(
                        rs.getInt("CategoryID")
                );

                c.setCategoryCode(
                        rs.getString("CategoryCode")
                );

                c.setCategoryName(
                        rs.getString("CategoryName")
                );

                c.setSlug(
                        rs.getString("Slug")
                );

                c.setDescription(
                        rs.getString("Description")
                );

                c.setImage(
                        rs.getString("Image")
                );

                c.setSortOrder(
                        rs.getInt("SortOrder")
                );

                c.setStatus(
                        rs.getString("Status")
                );


                return c;
            }


        } catch(Exception e){

            e.printStackTrace();

        }


        return null;
    }




    @Override
    public void save(Category c) {


        String sql = """
                INSERT INTO Categories
                (
                CategoryCode,
                CategoryName,
                Slug,
                Description,
                Image,
                SortOrder,
                Status
                )
                VALUES(?,?,?,?,?,?,?)
                """;


        try(Connection conn=getConnection();
            PreparedStatement ps=conn.prepareStatement(sql)) {


            ps.setString(1,c.getCategoryCode());

            ps.setString(2,c.getCategoryName());

            ps.setString(3,c.getSlug());

            ps.setString(4,c.getDescription());

            ps.setString(5,c.getImage());

            ps.setInt(6,c.getSortOrder());

            ps.setString(7,c.getStatus());


            ps.executeUpdate();


        }catch(Exception e){

            e.printStackTrace();

        }

    }




    @Override
    public void update(Category c) {


        String sql="""
                UPDATE Categories
                SET 
                CategoryCode=?,
                CategoryName=?,
                Slug=?,
                Description=?,
                Image=?,
                SortOrder=?,
                Status=?
                WHERE CategoryID=?
                """;


        try(Connection conn=getConnection();
            PreparedStatement ps=conn.prepareStatement(sql)){


            ps.setString(1,c.getCategoryCode());

            ps.setString(2,c.getCategoryName());

            ps.setString(3,c.getSlug());

            ps.setString(4,c.getDescription());

            ps.setString(5,c.getImage());

            ps.setInt(6,c.getSortOrder());

            ps.setString(7,c.getStatus());

            ps.setInt(8,c.getCategoryId());


            ps.executeUpdate();


        }catch(Exception e){

            e.printStackTrace();

        }

    }




    @Override
    public void delete(Integer id) {


        String sql =
                "DELETE FROM Categories WHERE CategoryID=?";


        try(Connection conn=getConnection();
            PreparedStatement ps=conn.prepareStatement(sql)){


            ps.setInt(1,id);

            ps.executeUpdate();


        }catch(Exception e){

            e.printStackTrace();

        }

    }




    @Override
    public List<Category> search(String keyword) {


        List<Category> list = new ArrayList<>();

        String sql="""
                SELECT *
                FROM Categories
                WHERE CategoryCode LIKE ?
                OR CategoryName LIKE ?
                ORDER BY CategoryID DESC
                """;


        try(Connection conn=getConnection();
            PreparedStatement ps=conn.prepareStatement(sql)){


            String key="%"+keyword+"%";


            ps.setString(1,key);
            ps.setString(2,key);


            ResultSet rs=ps.executeQuery();


            while(rs.next()){

                Category c=new Category();

                c.setCategoryId(rs.getInt("CategoryID"));

                c.setCategoryCode(
                        rs.getString("CategoryCode")
                );

                c.setCategoryName(
                        rs.getString("CategoryName")
                );

                c.setSlug(
                        rs.getString("Slug")
                );

                c.setDescription(
                        rs.getString("Description")
                );

                c.setSortOrder(
                        rs.getInt("SortOrder")
                );

                c.setStatus(
                        rs.getString("Status")
                );


                list.add(c);
            }


        }catch(Exception e){

            e.printStackTrace();

        }


        return list;
    }

}