package com.voting.controller;

import com.voting.dao.UserDao;
import com.voting.model.User;
import com.voting.service.IdentityVerificationFactory;
import com.voting.service.IdentityVerificationService;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String voterId = request.getParameter("voterId");
        String documentType = request.getParameter("documentType");
        String documentNumber = request.getParameter("documentNumber");
        String password = request.getParameter("password");

        if (!isValidEmail(email)) {
            response.sendRedirect("register.jsp?error=RegistrationFailed");
            return;
        }
        if (voterId == null || !voterId.trim().toUpperCase().matches("[A-Z0-9-]{6,20}")) {
            response.sendRedirect("register.jsp?error=InvalidVoterId");
            return;
        }
        if (!isStrongPassword(password)) {
            response.sendRedirect("register.jsp?error=WeakPassword");
            return;
        }

        if (documentNumber == null
                || !isValidDocumentNumber(documentType, documentNumber)) {
            response.sendRedirect("register.jsp?error=InvalidIdentityFormat");
            return;
        }
        documentNumber = documentNumber.trim().toUpperCase();
        voterId = voterId.trim().toUpperCase();

        String mode = System.getenv("IDENTITY_VERIFICATION_MODE");
        if ("DIGILOCKER".equalsIgnoreCase(mode)) {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("digilockerVerified") == null
                    || session.getAttribute("digilockerId") == null) {
                response.sendRedirect("register.jsp?error=IdentityVerificationRequired");
                return;
            }
            String verifiedId = String.valueOf(session.getAttribute("digilockerId")).trim();
            if (!verifiedId.equalsIgnoreCase(documentNumber)) {
                response.sendRedirect("register.jsp?error=IdentityVerificationFailed");
                return;
            }
            session.removeAttribute("digilockerVerified");
            session.removeAttribute("digilockerId");
            session.removeAttribute("digilockerName");
        } else {
            IdentityVerificationService verifier = IdentityVerificationFactory.create();
            try {
                if (!verifier.verify(documentType, documentNumber, voterId, name)) {
                    response.sendRedirect("register.jsp?error=IdentityVerificationFailed");
                    return;
                }
            } catch (IllegalStateException e) {
                response.sendRedirect("register.jsp?error=IdentityVerificationUnavailable");
                return;
            }
        }

        User newUser = new User(0, name, email, voterId, documentNumber, password, "voter", false);
        UserDao userDao = new UserDao();

        if (userDao.registerUser(newUser)) {
            response.sendRedirect("index.jsp?registered=true");
        } else {
            response.sendRedirect("register.jsp?error=RegistrationFailed");
        }
    }

    private boolean isValidDocumentNumber(String type, String number) {
        if (type == null || number == null) return false;
        if ("PAN".equalsIgnoreCase(type)) {
            return number.trim().toUpperCase().matches("[A-Z]{5}[0-9]{4}[A-Z]");
        }
        if ("AADHAAR".equalsIgnoreCase(type)) {
            return isValidAadhaarChecksum(number.trim());
        }
        return false;
    }

    private boolean isStrongPassword(String password) {
        if (password == null || password.length() < 16) return false;
        boolean upper = password.matches(".*[A-Z].*");
        boolean lower = password.matches(".*[a-z].*");
        boolean digit = password.matches(".*[0-9].*");
        boolean special = password.matches(".*[^A-Za-z0-9].*");
        return upper && lower && digit && special;
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.length() > 255) return false;
        String value = email.trim();
        return value.matches("^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
                && value.indexOf('\r') < 0
                && value.indexOf('\n') < 0;
    }

    private boolean isValidAadhaarChecksum(String aadhaar) {
        if (!aadhaar.matches("[2-9][0-9]{11}")) return false;
        int[][] d = {
            {0,1,2,3,4,5,6,7,8,9},
            {1,2,3,4,0,6,7,8,9,5},
            {2,3,4,0,1,7,8,9,5,6},
            {3,4,0,1,2,8,9,5,6,7},
            {4,0,1,2,3,9,5,6,7,8},
            {5,9,8,7,6,0,1,2,3,4},
            {6,8,9,5,4,1,2,3,0,7},
            {7,9,5,6,8,2,3,4,1,0},
            {8,5,6,7,9,3,4,0,1,2},
            {9,6,7,8,5,4,0,1,2,3}
        };
        int[][] p = {
            {0,1,2,3,4,5,6,7,8,9},
            {1,5,7,6,2,8,3,0,9,4},
            {5,8,6,3,7,9,0,1,4,2},
            {8,9,3,0,6,4,1,5,2,7},
            {9,4,0,1,3,2,5,8,7,6},
            {4,2,1,5,0,7,8,6,9,3},
            {2,7,5,8,1,6,9,3,4,0},
            {7,6,8,9,5,3,4,0,2,1}
        };
        int checksum = 0;
        for (int i = aadhaar.length() - 1, j = 0; i >= 0; i--, j++) {
            int digit = Character.getNumericValue(aadhaar.charAt(i));
            checksum = d[checksum][p[j % 8][digit]];
        }
        return checksum == 0;
    }
}
