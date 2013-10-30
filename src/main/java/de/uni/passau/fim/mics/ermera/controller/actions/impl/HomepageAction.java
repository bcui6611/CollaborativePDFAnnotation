package de.uni.passau.fim.mics.ermera.controller.actions.impl;

import de.uni.passau.fim.mics.ermera.controller.actions.Action;
import de.uni.passau.fim.mics.ermera.dao.content.ContentRepositoryDao;
import de.uni.passau.fim.mics.ermera.dao.content.ContentRepositoryDaoImpl;
import de.uni.passau.fim.mics.ermera.model.IndexBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class HomepageAction implements Action {
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {

        ContentRepositoryDao contentRepositoryDao = new ContentRepositoryDaoImpl();
        List<String> fileIds = contentRepositoryDao.getAllFileIDs();

        IndexBean indexBean = new IndexBean();
        indexBean.setFileIds(fileIds);

        request.setAttribute("indexBean", indexBean);
        request.getSession().removeAttribute("documentBean");

        return "homepage";
    }
}
