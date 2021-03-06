package com.car;

/**
 * Created by michael on 2015-05-04.
 */
//import com.mongodb.*;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.ErrorCategory;
import com.mongodb.MongoWriteException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import sun.misc.BASE64Encoder;

import com.mongodb.*;
import sun.misc.BASE64Encoder;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;


import static com.mongodb.client.model.Filters.eq;

public class UserDAO {
    private final MongoCollection<Document> usersCollection;
    private Random random = new SecureRandom();

    public UserDAO(final MongoDatabase blogDatabase) {
        usersCollection = blogDatabase.getCollection("users");
    }

    // validates that username is unique and insert into db
    public boolean addUser(String username, String password, String email) {

        String passwordHash = makePasswordHash(password, Integer.toString(random.nextInt()));

//        DBObject newUser = new BasicDBObject("password", passwordHash).append("_id", username);
        Document user = new Document("_id", username)
                .append("password",passwordHash);

//        if (email != null && !email.equals("")) {
//            // XXX WORK HERE
//            // if there is an email address specified, add it to the document too.
//        }

        try {
            System.out.println(user.toJson());
            usersCollection.insertOne(user);
            System.out.println(user.toJson());
//            usersCollection.insertOne(newUser);
            return true;
        } catch (MongoWriteException e) {
            if (e.getError().getCategory().equals(ErrorCategory.DUPLICATE_KEY)) {
                System.out.println("Username already in use: " + username);
                return false;
            }
            throw e;
        }
    }

    public Document validateLogin(String username, String password) {
        Document user = null;

        // XXX look in the user collection for a user that has this username
        // assign the result to the user variable.
        try{
            MongoCursor<Document> cursor = usersCollection.find(new BasicDBObject("_id", username)).iterator();
            user = cursor.next();
            System.out.println(user.toJson());
            //user = usersCollection.find(new BasicDBObject("_id", username));
        } catch (RuntimeException e){

        }
        if (user == null) {
            System.out.println("User not in database");
            return null;
        }

        String hashedAndSalted = user.get("password").toString();

        String salt = hashedAndSalted.split(",")[1];

        if (!hashedAndSalted.equals(makePasswordHash(password, salt))) {
            System.out.println("Submitted password is not a match");
            return null;
        }
        return user;
    }


    private String makePasswordHash(String password, String salt) {
        try {
            String saltedAndHashed = password + "," + salt;
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(saltedAndHashed.getBytes());
            BASE64Encoder encoder = new BASE64Encoder();
            byte hashedBytes[] = (new String(digest.digest(), "UTF-8")).getBytes();
            return encoder.encode(hashedBytes) + "," + salt;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 is not available", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 unavailable?  Not a chance", e);
        }
    }
}

