package com.utils.auth;

import com.utils.Helper;
import com.utils.data.QueryHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Auth {
    public static final String ID = "id";
    public static final String USER_ID = "userId";
    private static final String TOKEN = "token";
    public static final String USER_TOKEN = "userToken";
    private boolean isUser;
    private boolean isAdmin;

    public Auth(final HttpServletRequest request) {
        populate(request);
    }

    public boolean getIsAdmin() {
        return isAdmin;
    }

    public boolean getIsUser() {
        return isUser;
    }
    final void populate(final HttpServletRequest request) {
        var token = (Helper.isThing(request.getHeader(TOKEN))) ? request.getHeader(TOKEN) : request.getParameter(TOKEN);
        var id = QueryHelper.getIdByToken(token);
        if(Helper.isThing(id)) {
            isUser = true;
            isAdmin = QueryHelper.isAdmin(id);
        } else {
            isUser = false;
            isAdmin = false;
        }
    }
}
