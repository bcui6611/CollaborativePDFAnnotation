package de.uni.passau.fim.mics.ermera.controller.actions;

import de.uni.passau.fim.mics.ermera.controller.extractors.ExtractException;
import de.uni.passau.fim.mics.ermera.controller.extractors.Extractor;
import de.uni.passau.fim.mics.ermera.controller.extractors.knowminerPDFExtractor;
import de.uni.passau.fim.mics.ermera.dao.Storage;
import de.uni.passau.fim.mics.ermera.model.DocumentBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ExtractAction implements Action {

    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String id = request.getParameter("id");
        int pageNumber = 0;
        Storage storage = new Storage();
        DocumentBean documentBean = null;

        // get documentBean from storage, if none found extract it from file
        try {
            documentBean = storage.load(id);
        } catch (FileNotFoundException e) {
            // do nothing if only the file was not found..
        } catch (IOException e) {
            request.setAttribute("errorMessage", "Could not load saved file: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            request.setAttribute("errorMessage", "Corrupt save? Could not find class: " + e.getMessage());
        }

        if (documentBean == null) {
            try {
                Extractor extractor = new knowminerPDFExtractor();
                documentBean = extractor.extract(id);
            } catch (ExtractException e) {
                request.setAttribute("errorMessage", e.getMessage());
                return "extract";
            }
        }

        // get pageNumber Paramater
        if (request.getParameter("pageNumber") != null) {
            pageNumber = Integer.valueOf(request.getParameter("pageNumber")) - 1;
        }

        // react on action?
        //TODO errormessage handling after ajax?!
        String action = request.getParameter("action");
        if (action != null) {
            System.out.println("handling action: " + action);
            switch (action) {
                case "sort":
                    String[] items = request.getParameterValues("items[]");
                    if (items != null) {
                        documentBean.reorderPageBean(pageNumber, items);
                    }
                    break;
                case "unselect":
                    documentBean.unselectBlock(pageNumber, request.getParameter("item"));
                    break;
                case "toggleHeadline":
                    documentBean.toggleHeadline(pageNumber, request.getParameter("item"));
                    break;
                case "toggleNewParagraph":
                    documentBean.toggleNewParagraph(pageNumber, request.getParameter("item"));
                    break;
                default:
                    break;
            }
        }

        // finished everything.. store bean and also attach it to the request
        if (documentBean != null) {
            try {
                storage.store(documentBean);
            } catch (IOException e) {
                request.setAttribute("errorMessage", "Could not save DocumentBean: " + e.getMessage());
            }
            request.setAttribute("documentBean", documentBean);
        }

        return "extract";
    }
}