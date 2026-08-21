package com.zyrotech.service;


import com.google.gson.JsonObject;
import com.zyrotech.dto.UserDTO;
import com.zyrotech.entity.Address;
import com.zyrotech.entity.City;
import com.zyrotech.entity.Status;
import com.zyrotech.entity.User;
import com.zyrotech.mail.VerificationMail;
import com.zyrotech.provider.MailServiceProvider;
import com.zyrotech.util.AppUtil;
import com.zyrotech.util.HibernateUtil;
import com.zyrotech.validation.Validator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.core.Context;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class UserService {

    public String addNewUser(UserDTO userDTO) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message;
        Session hibernateSession = null;
        Transaction transaction = null;

        if (userDTO.getFname() == null || userDTO.getFname().isBlank()) {
            message = "First name is required!";
        } else if (userDTO.getLname() == null || userDTO.getLname().isBlank()) {
            message = "Last name is required!";
        } else if (userDTO.getEmail() == null || userDTO.getEmail().isBlank()) {
            message = "Email is required!";
        } else if (!userDTO.getEmail().matches(Validator.EMAIL_VALIDATION)) {
            message = "Please provide valid email address!";
        } else if (userDTO.getPassword() == null || userDTO.getPassword().isBlank()) {
            message = "Password is required!";
        } else if (!userDTO.getPassword().matches(Validator.PASSWORD_VALIDATION)) {
            message = "Password must be at least 8 characters with uppercase, lowercase, digit and special character!";
        } else if (userDTO.getLineOne() == null || userDTO.getLineOne().isBlank()) {
            message = "Address line one is required!";
        } else if (userDTO.getMobile() == null || userDTO.getMobile().isBlank()) {
            message = "Mobile number is required!";
        } else if (!userDTO.getMobile().matches(Validator.MOBILE_VALIDATION)) {
            message = "Please provide a valid mobile number!";
        } else if (userDTO.getCityId() == null || userDTO.getCityId() <= 0) {
            message = "Please select a city!";
        } else if (userDTO.getPostalCode() == null || userDTO.getPostalCode().isBlank()) {
            message = "Postal code is required!";
        } else {
            try {
                hibernateSession = HibernateUtil.getSessionFactory().openSession();

                User singleUser = hibernateSession.createNamedQuery("User.getByEmail", User.class)
                        .setParameter("email", userDTO.getEmail())
                        .getSingleResultOrNull();

                if (singleUser != null) {
                    message = "This email already exists! Please use another email";
                } else {
                    City city = hibernateSession.find(City.class, userDTO.getCityId());
                    if (city == null) {
                        message = "Selected city not found!";
                    } else {
                        transaction = hibernateSession.beginTransaction();

                        User u = new User();
                        u.setFname(userDTO.getFname());
                        u.setLname(userDTO.getLname());
                        u.setEmail(userDTO.getEmail());
                        u.setPassword(userDTO.getPassword());

                        String verificationCode = AppUtil.generateCode();
                        u.setVerificationCode(verificationCode);

                        Status pendingStatus = hibernateSession.createNamedQuery("Status.findByValue", Status.class)
                                .setParameter("value", String.valueOf(Status.Type.PENDING)).getSingleResult();
                        u.setStatus(pendingStatus);

                        hibernateSession.persist(u);

                        Address address = new Address();
                        address.setLineOne(userDTO.getLineOne());
                        address.setLineTwo(userDTO.getLineTwo());
                        address.setMobile(userDTO.getMobile());
                        address.setPostalCode(userDTO.getPostalCode());
                        address.setCity(city);
                        address.setUser(u);
                        address.setPrimary(true);
                        hibernateSession.persist(address);

                        transaction.commit();

                        VerificationMail verificationMail = new VerificationMail(u.getEmail(), verificationCode);
                        MailServiceProvider.getInstance().sendMail(verificationMail);

                        status = true;
                        message = "Account created successfully. Verification code has been sent to your email. " +
                                "Please verify to activate your account!";
                    }
                }
            } catch (HibernateException e) {
                if (transaction != null) {
                    transaction.rollback();
                }
                message = "Account creation failed. Please try again!";
            } finally {
                if (hibernateSession != null) {
                    hibernateSession.close();
                }
            }
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }

    public String userLogin(UserDTO userDTO, @Context HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message = "";

        if (userDTO.getEmail() == null || userDTO.getEmail().isBlank()) {
            message = "Email is required!";
        } else if (!userDTO.getEmail().matches(Validator.EMAIL_VALIDATION)) {
            message = "Please provide valid email address!";
        } else if (userDTO.getPassword() == null || userDTO.getPassword().isBlank()) {
            message = "Password is required!";
        } else {
            Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
            try {
                User singleUser = hibernateSession.createNamedQuery("User.getByEmail", User.class)
                        .setParameter("email", userDTO.getEmail())
                        .getSingleResultOrNull();

                if (singleUser == null) {
                    message = "Account not found. Please register first!";
                } else if (!singleUser.getPassword().equals(userDTO.getPassword())) {
                    message = "Invalid password! Please check your credentials!";
                } else {
                    Status verifiedStatus = hibernateSession.createNamedQuery("Status.findByValue", Status.class)
                            .setParameter("value", String.valueOf(Status.Type.VERIFIED))
                            .getSingleResult();

                    if (!singleUser.getStatus().equals(verifiedStatus)) {
                        message = "Your account is not verified. Please verify first!";
                    } else {
                        HttpSession httpSession = request.getSession();
                        httpSession.setAttribute("user", singleUser);
                        status = true;
                        message = "Login successful";
                    }
                }
            } finally {
                hibernateSession.close();
            }
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }

    public String verifyUserAccount(UserDTO userDTO) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message = "";

        if (userDTO.getEmail() == null || userDTO.getEmail().isBlank()) {
            message = "Email is required!";
        } else if (!userDTO.getEmail().matches(Validator.EMAIL_VALIDATION)) {
            message = "Please provide valid email address!";
        } else if (userDTO.getVerificationCode() == null || userDTO.getVerificationCode().isBlank()) {
            message = "Verification code is required!";
        } else if (!userDTO.getVerificationCode().matches(Validator.VERIFICATION_CODE_VALIDATION)) {
            message = "Verification code must be 6 digits!";
        } else {
            Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
            Transaction transaction = null;
            try {
                User user = hibernateSession.createQuery(
                                "FROM User u WHERE u.email=:email AND u.verificationCode=:verificationCode", User.class)
                        .setParameter("email", userDTO.getEmail())
                        .setParameter("verificationCode", userDTO.getVerificationCode())
                        .getSingleResultOrNull();

                if (user == null) {
                    message = "Invalid verification code!";
                } else {
                    Status verifiedStatus = hibernateSession.createNamedQuery("Status.findByValue", Status.class)
                            .setParameter("value", String.valueOf(Status.Type.VERIFIED))
                            .getSingleResult();

                    if (user.getStatus().equals(verifiedStatus)) {
                        message = "Account already verified!";
                    } else {
                        transaction = hibernateSession.beginTransaction();
                        user.setStatus(verifiedStatus);
                        user.setVerificationCode("");
                        hibernateSession.merge(user);
                        transaction.commit();
                        status = true;
                        message = "Account verification completed successfully!";
                    }
                }
            } catch (HibernateException e) {
                if (transaction != null) {
                    transaction.rollback();
                }
                message = "Verification failed. Please try again!";
            } finally {
                hibernateSession.close();
            }
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }
}