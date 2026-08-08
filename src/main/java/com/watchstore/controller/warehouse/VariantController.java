package com.watchstore.controller.warehouse;

import com.watchstore.model.Variant;
import com.watchstore.repository.VariantRepository;
import com.watchstore.util.ViewRouter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;

@WebServlet(urlPatterns = {
        "/manage/warehouse/variants",
        "/manage/warehouse/variant-form",
        "/manage/warehouse/variant-create",
        "/manage/warehouse/variant-update",
        "/manage/warehouse/variant-delete",
        "/manage/warehouse/variant-status"
})
public class VariantController extends HttpServlet {

    private VariantRepository variantRepo;

    @Override
    public void init() {
        variantRepo = new VariantRepository();
    }

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws ServletException, IOException {

        String path = req.getServletPath();

        try {

            switch (path) {

                case "/manage/warehouse/variants":

                    handleVariantList(req);

                    render(
                            req,
                            resp,
                            "variant",
                            "Biến thể sản phẩm"
                    );

                    return;

                case "/manage/warehouse/variant-form":

                    handleVariantForm(req);

                    render(
                            req,
                            resp,
                            "variant-form",
                            "Biến thể sản phẩm"
                    );

                    return;

                default:

                    resp.sendError(
                            HttpServletResponse.SC_NOT_FOUND
                    );
            }

        } catch (Exception e) {

            e.printStackTrace();

            req.getSession().setAttribute(
                    "errorMsg",
                    getErrorMessage(e)
            );

            resp.sendRedirect(
                    req.getContextPath()
                            + "/manage/warehouse/variants"
            );
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws ServletException, IOException {

        String path = req.getServletPath();

        try {

            switch (path) {

                case "/manage/warehouse/variant-create":

                    handleVariantCreate(
                            req,
                            resp
                    );

                    return;

                case "/manage/warehouse/variant-update":

                    handleVariantUpdate(
                            req,
                            resp
                    );

                    return;

                case "/manage/warehouse/variant-delete":

                    handleVariantDelete(
                            req,
                            resp
                    );

                    return;

                case "/manage/warehouse/variant-status":

                    handleVariantStatus(
                            req,
                            resp
                    );

                    return;

                default:

                    resp.sendError(
                            HttpServletResponse.SC_NOT_FOUND
                    );
            }

        } catch (Exception e) {

            e.printStackTrace();

            req.getSession().setAttribute(
                    "errorMsg",
                    getErrorMessage(e)
            );

            String variantId =
                    req.getParameter("variantId");

            if (variantId != null &&
                    !variantId.trim().isEmpty()) {

                resp.sendRedirect(
                        req.getContextPath()
                                + "/manage/warehouse/variant-form?id="
                                + variantId
                );

            } else {

                resp.sendRedirect(
                        req.getContextPath()
                                + "/manage/warehouse/variants"
                );
            }
        }
    }

    private void handleVariantList(
            HttpServletRequest req
    ) {

        String keyword =
                optionalString(
                        req.getParameter("keyword")
                );

        String status =
                optionalString(
                        req.getParameter("status")
                );

        req.setAttribute(
                "keyword",
                keyword
        );

        req.setAttribute(
                "status",
                status
        );

        req.setAttribute(
                "variants",
                variantRepo.search(
                        keyword,
                        status
                )
        );
    }

    private void handleVariantForm(
            HttpServletRequest req
    ) throws Exception {

        req.setAttribute(
                "productOptions",
                variantRepo.findProductOptions()
        );

        String idParam =
                req.getParameter("id");

        if (idParam == null ||
                idParam.trim().isEmpty()) {

            return;
        }

        int variantId =
                parsePositiveInt(
                        idParam,
                        "ID biến thể không hợp lệ."
                );

        Variant variant =
                variantRepo.findById(
                        variantId
                );

        if (variant == null) {

            throw new Exception(
                    "Không tìm thấy biến thể."
            );
        }

        req.setAttribute(
                "variant",
                variant
        );
    }

    private void handleVariantCreate(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws Exception {

        Variant variant =
                buildVariantFromRequest(
                        req
                );

        variantRepo.create(
                variant
        );

        req.getSession().setAttribute(
                "successMsg",
                "Thêm biến thể thành công."
        );

        resp.sendRedirect(
                req.getContextPath()
                        + "/manage/warehouse/variants"
        );
    }

    private void handleVariantUpdate(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws Exception {

        Variant variant =
                buildVariantFromRequest(
                        req
                );

        variant.setVariantId(
                parsePositiveInt(
                        req.getParameter("variantId"),
                        "ID biến thể không hợp lệ."
                )
        );

        variantRepo.update(
                variant
        );

        req.getSession().setAttribute(
                "successMsg",
                "Cập nhật biến thể thành công."
        );

        resp.sendRedirect(
                req.getContextPath()
                        + "/manage/warehouse/variants"
        );
    }

    private void handleVariantDelete(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws Exception {

        int variantId =
                parsePositiveInt(
                        req.getParameter("variantId"),
                        "ID biến thể không hợp lệ."
                );

        variantRepo.deactivate(
                variantId
        );

        req.getSession().setAttribute(
                "successMsg",
                "Đã xử lý xóa biến thể."
        );

        resp.sendRedirect(
                req.getContextPath()
                        + "/manage/warehouse/variants"
        );
    }

    private void handleVariantStatus(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws Exception {

        int variantId =
                parsePositiveInt(
                        req.getParameter("variantId"),
                        "ID biến thể không hợp lệ."
                );

        String status =
                optionalString(
                        req.getParameter("status")
                );

        if (status == null) {

            throw new Exception(
                    "Trạng thái biến thể không được để trống."
            );
        }

        variantRepo.setStatus(
                variantId,
                status
        );

        req.getSession().setAttribute(
                "successMsg",
                "Cập nhật trạng thái biến thể thành công."
        );

        resp.sendRedirect(
                req.getContextPath()
                        + "/manage/warehouse/variants"
        );
    }

    private Variant buildVariantFromRequest(
            HttpServletRequest req
    ) throws Exception {

        Variant variant =
                new Variant();

        variant.setProductId(
                parsePositiveInt(
                        req.getParameter("productId"),
                        "Phải chọn sản phẩm."
                )
        );

        variant.setVariantName(
                requiredString(
                        req.getParameter("variantName"),
                        "Tên biến thể không được để trống."
                )
        );

        variant.setSku(
                requiredString(
                        req.getParameter("sku"),
                        "SKU không được để trống."
                )
        );

        variant.setBarcode(
                optionalString(
                        req.getParameter("barcode")
                )
        );

        variant.setCostPrice(
                parseMoney(
                        req.getParameter("costPrice"),
                        "Giá nhập không hợp lệ."
                )
        );

        variant.setSalePrice(
                parseMoney(
                        req.getParameter("salePrice"),
                        "Giá bán không hợp lệ."
                )
        );

        String compareAtPrice =
                optionalString(
                        req.getParameter("compareAtPrice")
                );

        if (compareAtPrice != null) {

            variant.setCompareAtPrice(
                    parseMoney(
                            compareAtPrice,
                            "Giá niêm yết không hợp lệ."
                    )
            );
        }

        String weightGram =
                optionalString(
                        req.getParameter("weightGram")
                );

        if (weightGram != null) {

            variant.setWeightGram(
                    parsePositiveOrZeroInt(
                            weightGram,
                            "Trọng lượng không hợp lệ."
                    )
            );
        }

        String status =
                optionalString(
                        req.getParameter("status")
                );

        if (status == null) {
            status = "ACTIVE";
        }

        variant.setStatus(
                status
        );

        return variant;
    }

    private void render(
            HttpServletRequest req,
            HttpServletResponse resp,
            String page,
            String title
    ) throws ServletException, IOException {

        req.setAttribute(
                "cp",
                req.getContextPath()
        );

        req.setAttribute(
                "moduleTitle",
                title
        );

        ViewRouter.admin(
                req,
                resp,
                "warehouse/" + page,
                title,
                "warehouse"
        );
    }

    private int parsePositiveInt(
            String value,
            String message
    ) throws Exception {

        try {

            if (value == null ||
                    value.trim().isEmpty()) {

                throw new Exception(message);
            }

            int result =
                    Integer.parseInt(
                            value.trim()
                    );

            if (result <= 0) {

                throw new Exception(message);
            }

            return result;

        } catch (NumberFormatException e) {

            throw new Exception(message);
        }
    }

    private int parsePositiveOrZeroInt(
            String value,
            String message
    ) throws Exception {

        try {

            if (value == null ||
                    value.trim().isEmpty()) {

                throw new Exception(message);
            }

            int result =
                    Integer.parseInt(
                            value.trim()
                    );

            if (result < 0) {

                throw new Exception(message);
            }

            return result;

        } catch (NumberFormatException e) {

            throw new Exception(message);
        }
    }

    private BigDecimal parseMoney(
            String value,
            String message
    ) throws Exception {

        try {

            if (value == null ||
                    value.trim().isEmpty()) {

                throw new Exception(message);
            }

            BigDecimal result =
                    new BigDecimal(
                            value.trim()
                    );

            if (result.compareTo(
                    BigDecimal.ZERO
            ) < 0) {

                throw new Exception(message);
            }

            return result;

        } catch (NumberFormatException e) {

            throw new Exception(message);
        }
    }

    private String requiredString(
            String value,
            String message
    ) throws Exception {

        if (value == null ||
                value.trim().isEmpty()) {

            throw new Exception(message);
        }

        return value.trim();
    }

    private String optionalString(
            String value
    ) {

        if (value == null ||
                value.trim().isEmpty()) {

            return null;
        }

        return value.trim();
    }

    private String getErrorMessage(
            Exception e
    ) {

        if (e.getMessage() == null ||
                e.getMessage().trim().isEmpty()) {

            return "Có lỗi xảy ra trong quá trình xử lý.";
        }

        return e.getMessage();
    }
}