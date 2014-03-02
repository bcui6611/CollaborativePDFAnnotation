package de.uni.passau.fim.mics.ermera.controller.actions.impl;

import de.uni.passau.fim.mics.ermera.common.MessageTypes;
import de.uni.passau.fim.mics.ermera.controller.Views;
import de.uni.passau.fim.mics.ermera.controller.actions.AbstractAction;
import de.uni.passau.fim.mics.ermera.controller.actions.ActionException;
import de.uni.passau.fim.mics.ermera.dao.DocumentDao;
import de.uni.passau.fim.mics.ermera.dao.DocumentDaoException;
import de.uni.passau.fim.mics.ermera.dao.DocumentDaoImpl;
import de.uni.passau.fim.mics.ermera.model.DocumentBean;
import de.uni.passau.fim.mics.ermera.opennlp.*;
import opennlp.tools.namefind.TokenNameFinderModel;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NLPAction extends AbstractAction {
    private static final Logger LOGGER = Logger.getLogger(NLPAction.class);

    @Override
    public String executeConcrete(HttpServletRequest request, HttpServletResponse response) throws ActionException {
        String[] files = request.getParameterValues("files");
        if (files == null) {
            mu.addMessage(MessageTypes.ERROR, "no files selected");
            return null;
        }

        if (request.getParameter("create") != null) {
            return handleCreateModel(request, userid, files);
        } else if (request.getParameter("use") != null) {
            return handleUseModel(request, session, userid, files);
        } else {
           return Views.HOMEPAGE.toString();
        }
    }

    private String handleUseModel(HttpServletRequest request, HttpSession session, String userid, String[] files) throws ActionException {
        String modelname = request.getParameter("modelselect");
        if (modelname == null) {
            mu.addMessage(MessageTypes.ERROR, "no modelname entered");
            return null;
        }

        List<NameFinderResult> list;
        try {
            list = useModel(userid, modelname, files);
        } catch (NLPException e) {
            throw new ActionException("Fehler beim Anwenden des NLP Modells", e);
        }
        session.setAttribute("resultList", list);
        return Views.EVALUATION.toString();
    }

    private String handleCreateModel(HttpServletRequest request, String userid, String[] files) throws ActionException {
        String modelname = request.getParameter("modelname");
        String entitiytype = request.getParameter("entitiytype");
        if (modelname == null) {
            mu.addMessage(MessageTypes.ERROR, "no modelname entered");
            return null;
        }
        if (entitiytype == null) {
            mu.addMessage(MessageTypes.ERROR, "no entitiytype entered");
            return null;
        }

        createModel(userid, modelname, entitiytype, files);
        mu.addMessage(MessageTypes.SUCCESS, "model created");
        return Views.HOMEPAGE.toString();
    }

    @SuppressWarnings({"UnusedParameters"})
    private void createModel(String userid, String modelname, String entitiytype, String[] files) throws ActionException {
        try {
            //TODO: select documents which should form a new model
            OpenNLPService nlpService = new OpenNLPServiceImpl();
            TokenNameFinderModel model = nlpService.train(userid, entitiytype);

            // store model
            DocumentDao documentDao = new DocumentDaoImpl();
            documentDao.storeModel(userid, modelname, model, entitiytype);
        } catch (IOException e) {
            throw new ActionException("Fehler beim erzeugen des Models", e);
        }
    }

    private List<NameFinderResult> useModel(String userid, String modelname, String[] files) throws NLPException {
        OpenNLPService nlpService = new OpenNLPServiceImpl();

        // model holen
        DocumentDao documentDao = new DocumentDaoImpl();
        ModelEntity model = null;
        try {
            model = documentDao.loadModel(userid, modelname);
        } catch (IOException e) {
            LOGGER.error("IO error loading the model", e);
            mu.addMessage(MessageTypes.ERROR, "Error loading the model: " + e.getMessage());
        }
        if (model == null) {
            throw new NLPException("Unexpected! Model must not be null!");
        }

        //get files as texts
        Map<String, String> documentStrs = new HashMap<>();
        for (String filename : files) {
            DocumentBean documentBean;
            try {
                documentBean = documentDao.loadDocumentBean(userid, filename);
            } catch (DocumentDaoException e) {
                LOGGER.error("error while loading documentBean", e);
                mu.addMessage(MessageTypes.ERROR, "error while loading documentBean: " + e.getMessage());
                continue;
            }

            documentStrs.put(documentBean.getId(), documentBean.toString());
        }

        // find entities
        return nlpService.find(model, documentStrs);
    }
}