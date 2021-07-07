package controller;

import model.Cart;
import model.CartItem;
import model.Categoria.CategoriaDAO;
import model.Prodotto.Prodotto;
import model.Prodotto.ProdottoDAO;
import model.Prodotto.ProductBuilder;

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
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ServletContext context = request.getServletContext();
        List<Prodotto> products = (List<Prodotto>) context.getAttribute("listProducts");

        try {
            String path = (request.getPathInfo() == null ? "/" : request.getPathInfo());
            HttpSession session = request.getSession();
            Cart cart = null;
            Prodotto p = null;
            ProdottoDAO dao = new ProdottoDAO();
            CategoriaDAO categoriaDAO = new CategoriaDAO();
                cart = new Cart(new ArrayList<>());
                if (session.getAttribute("sessionCart") == null) {
                session.setAttribute("sessionCart", cart);
            } else {
                cart = (Cart) session.getAttribute("sessionCart");
            }

            switch (path) {
                case "/select":
                    RequestValidator.authenticate(session, "userSession");
                    String productId = request.getParameter("productId");
                    String quantity = request.getParameter("fieldQuantity");
                    int id = Integer.parseInt(productId);
                    CartItem item = dao.doRetrieveCartItemById(id);
                    System.out.println("id:" + id + " " + (item != null));
                    if (item != null) {
                        cart.addProduct(item.getProdotto(), Integer.parseInt(quantity));
                        System.out.println("id: " + item.getProdotto().getId() + "  prezzo scontato: " + item.getProdotto().getPrezzo());
                    }
                    request.getRequestDispatcher("/WEB-INF/results/account.jsp").forward(request, response);
                    break;
                case "/create":
                    RequestValidator.authorize(session, "userSession");
                    Part part = request.getPart("productImage");
                    String fileName = Paths.get(part.getSubmittedFileName()).getFileName().toString();
                    String category = request.getParameter("productCategory");
                    Map<String, String[]> map = request.getParameterMap();
                    Set<Map.Entry<String, String[]>> set = map.entrySet();
                    for (Map.Entry<String, String[]> entry : set) {
                        System.out.println(entry.getKey() + "   " + Arrays.toString(entry.getValue()));
                    }
                    System.out.println("Categoria: " + category);
                    File file = null;
                    try (InputStream fileStream = part.getInputStream()) {
                        file = new File(uploadRoot + fileName);
                        if (!file.exists())
                            Files.copy(fileStream, file.toPath());
                    }

                    String resource = "/WEB-INF/results/manage-products.jsp";
                    switch (category) {
                        case "Materiale plastico":
                            System.out.println("file name: " + fileName);
                            p = ProductBuilder.createMaterialePlastico(map, fileName);
                            p.setCategoria(categoriaDAO.doRetrieveByName(map.get("productCategory")[0]));
                            dao.doSave(p);
                            if (products != null)
                                products.add(p);
                            request.getRequestDispatcher(resource).forward(request, response);
                            break;
                        case "Ricambi":
                            p = ProductBuilder.createRicambio(map, fileName);
                            p.setCategoria(categoriaDAO.doRetrieveByName(map.get("productCategory")[0]));
                            dao.doSave(p);
                            if (products != null)
                                products.add(p);
                            request.getRequestDispatcher(resource).forward(request, response);
                            break;
                        case "Accessori":
                            p = ProductBuilder.createAccessorio(map, fileName);
                            p.setCategoria(categoriaDAO.doRetrieveByName(map.get("productCategory")[0]));
                            dao.doSave(p);
                            if (products != null)
                                products.add(p);
                            request.getRequestDispatcher(resource).forward(request, response);
                            break;
                        case "Utensili":
                            p = ProductBuilder.createUtensile(map, fileName);
                            p.setCategoria(categoriaDAO.doRetrieveByName(map.get("productCategory")[0]));
                            dao.doSave(p);
                            if (products != null)
                                products.add(p);
                            request.getRequestDispatcher(resource).forward(request, response);
                            break;
                        case "Stampanti 3D":
                            p = ProductBuilder.createStampante3D(map, fileName);
                            p.setCategoria(categoriaDAO.doRetrieveByName(map.get("productCategory")[0]));
                            dao.doSave(p);
                            if (products != null)
                                products.add(p);
                            request.getRequestDispatcher(resource).forward(request, response);
                            break;
                        case "Resine":
                            p = ProductBuilder.createResina(map, fileName);
                            p.setCategoria(categoriaDAO.doRetrieveByName(map.get("productCategory")[0]));
                            dao.doSave(p);
                            if (products != null)
                                products.add(p);
                            request.getRequestDispatcher(resource).forward(request, response);
                            break;
                        default:
                            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                            break;
                    }
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}