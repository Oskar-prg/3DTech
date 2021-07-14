package controller;


import model.Categoria.CategoriaDAO;

import model.Prodotto.Prodotto;
import model.Prodotto.ProdottoDAO;
import model.Prodotto.ProductBuilder;
import model.utilities.RequestNotValidException;
import model.utilities.RequestValidator;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

@MultipartConfig
@WebServlet(urlPatterns = "/product/*")
public class ProductServlet extends HttpServlet {
    private static String uploadRoot;

    @Override
    public void init() throws ServletException {
        super.init();
        uploadRoot = FileServlet.getUploadPath() + File.separator + "special_folder" + File.separator;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = (request.getPathInfo() == null ? "/" : request.getPathInfo());
        ProdottoDAO dao = new ProdottoDAO();
        String resource = null;


        if (path.equals("/product-info")) {
            String option = request.getParameter("option");
            if (!Pattern.compile("^(?=.*[^\\s])\\d*$").matcher(option).matches()) {
                resource = "/index.jsp";
                request.setAttribute("errorMessage", "Prodotto inesistente");
                request.getRequestDispatcher(resource).forward(request, response);
                return;
            }

            int id = Integer.parseInt(option);
            Prodotto prodotto = dao.doRetrieveById(id);
            if (prodotto != null) {
                request.setAttribute("back", "/product/product-info?option=" + option);
                request.setAttribute("product", prodotto);
                resource = "/WEB-INF/results/productinfo.jsp";
            } else {
                request.setAttribute("errorMessage", "Prodotto inesistente");
                resource = "/index.jsp";
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        request.getRequestDispatcher(resource).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = (request.getPathInfo() == null ? "/" : request.getPathInfo());
        ServletContext context = request.getServletContext();
        List<Prodotto> products = (List<Prodotto>) context.getAttribute("listProducts");
        String back = request.getParameter("test");
        System.out.println(back);
        HttpSession session = request.getSession();
        ProdottoDAO dao = new ProdottoDAO();
        CategoriaDAO categoriaDAO = new CategoriaDAO();
        Part part;
        String fileName = "";
        File file;

        try {
            switch (path) {
                case "/update":
                    String str;
                    RequestValidator.authorize(session, "userSession");
                    part = request.getPart("productImage");
                    Map<String, String[]> mappa = request.getParameterMap();
                    String productId = request.getParameter("product-id");
                    int id = Integer.parseInt(productId);

                    if (part == null) {
                        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        return;
                    }

                    str = Paths.get(part.getSubmittedFileName()).getFileName().toString();
                    if (str.isEmpty()) {
                        fileName = request.getParameter("fieldImage");
                    } else {
                        fileName = str;
                    }

                    try (InputStream fileStream = part.getInputStream()) {
                        file = new File(uploadRoot + fileName);
                        if (!file.exists())
                            Files.copy(fileStream, file.toPath());
                    }

                    Prodotto p = ProductBuilder.getProduct(mappa, fileName);
                    p.setId(id);
                    p.setCategoria(categoriaDAO.doRetrieveByName(mappa.get("productCategory")[0]));
                    Optional<Prodotto> opt = products.stream().
                            filter(prodotto -> prodotto.getId() == id).
                            findFirst();
                    if (opt.isPresent()) {
                        int index = products.indexOf(opt.get());
                        products.set(index, p);
                        if (dao.doUpdateById(p)) {
                            request.getRequestDispatcher("/WEB-INF/results/manage-products.jsp").forward(request, response);
                        } else {
                            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        }
                    } else {
                        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    }
                    break;
                case "/create":
                    RequestValidator.authorize(session, "userSession");
                    part = request.getPart("productImage");
                    fileName = Paths.get(part.getSubmittedFileName()).getFileName().toString();
                    Map<String, String[]> map = request.getParameterMap();

                    try (InputStream fileStream = part.getInputStream()) {
                        file = new File(uploadRoot + fileName);
                        if (!file.exists())
                            Files.copy(fileStream, file.toPath());
                    }
                    String resource = "/WEB-INF/results/manage-products.jsp";
                    Prodotto product = ProductBuilder.getProduct(map, fileName);

                    product.setCategoria(categoriaDAO.doRetrieveByName(map.get("productCategory")[0]));
                    dao.doSave(product);

                    if (products != null) {
                        products.add(product);
                        request.getRequestDispatcher(resource).forward(request, response);
                    }
                    break;
                case "/remove":
                    RequestValidator.authorize(session, "userSession");
                    System.out.println("remove product");
                    String userId1 = request.getParameter("userId");
                    int id1 = Integer.parseInt(userId1);
                    Optional<Prodotto> opt1 = products.stream().
                            filter(prodotto -> prodotto.getId() == id1).
                            findFirst();

                    if (opt1.isPresent()) {
                        if (dao.doDeleteById(id1)) {
                            Prodotto p1 = opt1.get();
                            p1.setVisible(false);
                            response.sendRedirect(context.getContextPath() + "/controlpanel/products");
                        } else {
                            System.out.println("delete product error");
                        }
                    } else {
                        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    }
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    break;
            }
        } catch (RequestNotValidException e) {
            e.dispatchErrors(request, response);
        }
    }
}