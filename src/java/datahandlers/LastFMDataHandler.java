/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package datahandlers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.util.logging.Logger;
import java.util.logging.Level;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import objectModels.LastFMUser;
import org.xml.sax.SAXException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import processes.AppLogger;
import processes.ConfigParameters;
import objectModels.User;
import objectModels.Tag;
import objectModels.LastFMUser;
import objectModels.Artist;

/**
 *
 * @author Sajith
 */
public class LastFMDataHandler {

    private static final Logger LOGGER
            = AppLogger.getNewLogger(AccessLastFM.class.getName());

    private static int currentTagID = 1;
    private static int currentUserID = 1;

    
    private static HashMap<String, User> initialUsers = new HashMap<>();
    //Stores tag name as key and user object as the value
    private static HashMap<String, Tag> initialTags = new HashMap<>();
    //Stores tag name as key and user object as the value
    private static HashMap<String, Artist> initialArtists = new HashMap<>();

    public static void initiateUsers() {
        loadPreviousData();
        for (String user : getBaseUsers()) {
            LastFMUser tempUser = new LastFMUser(user);//Initates the LastFm User
            for (String artist : getUserArtistList(user)) {
                if (!initialArtists.containsKey(artist)) {
                    //If the artist is not initated initated the artists
                    initiateArtist(artist);
                }
                for (Tag tag : initialArtists.get(artist).getTagList()) {
                    //adds each artists tags to user to generate user taste
                    tempUser.addTag(tag);
                }
            }
            tempUser.filterTaste();
            initialUsers.put(currentUserID+"",tempUser);
            currentUserID++;
        }
    }

    private static void initiateArtist(String artist) {
        Artist tempArtist = new Artist(artist);
        List<Tag> artistTags = new ArrayList<>();
        try {
            for (String tag : getArtistTags(artist)) {
                if (!initialTags.containsKey(tag)) {
                    Tag tempTag = new Tag(currentTagID++, tag);
                    initialTags.put(tag, tempTag);
                }
                artistTags.add(initialTags.get(tag));
            }
        } catch (Exception e) {
            System.out.println("initiate artist failed [" + artist + "]");
        }
        tempArtist.addTagSet(artistTags);
        initialArtists.put(artist, tempArtist);
    }

    //Returns a string list of users
    public static List<String> getBaseUsers() {
        String baseUser = ConfigParameters.configParameter().getParameter("lastFMUserName");
        int userCount = Integer.parseInt(
                ConfigParameters.configParameter().getParameter("initialUserCount"));
        String method = "user.getFriends"
                + "&user=" + baseUser;
        List<String> usersList = null;
        try {
            URL url = AccessLastFM.getURL(method, userCount);
            Document usersListXML = AccessLastFM.getResponse(url);
            NodeList usersInfo = AccessLastFM.getElementList(usersListXML, "user");
            usersList = AccessLastFM.extractAttributes(usersInfo, "name");
        } catch (IOException | ParserConfigurationException | SAXException ex) {
            String msg = "Base user url curropted or unreachable";
            LOGGER.log(Level.SEVERE, msg, ex);
        }
        return usersList;
    }

    //Returns a string list of Artist's name for the given user
    public static List<String> getUserArtistList(String user) {
        int artistCount = Integer.parseInt(
                ConfigParameters.configParameter().getParameter("artistCountPerUser"));
        String method = "user.getTopArtists"
                + "&user=" + user;
        List<String> artistList = null;
        try {
            URL url = AccessLastFM.getURL(method, artistCount);
            Document artistListXML = AccessLastFM.getResponse(url);
            NodeList artistsInfo = AccessLastFM.getElementList(artistListXML, "artist");
            artistList = AccessLastFM.extractAttributes(artistsInfo, "name");
        } catch (IOException | ParserConfigurationException | SAXException ex) {
            String msg = "Users artistList unreachable or currupted";
            LOGGER.log(Level.SEVERE, msg, ex);
        }
        return artistList;
    }

    //Returns a string list of tags for the given artist
    public static List<String> getArtistTags(String artistName) {
        int artistTagCount = Integer.parseInt(
                ConfigParameters.configParameter().getParameter("tagCountPerArtist"));
        String method = "artist.getTopTags"
                + "&artist=" + artistName;
        List<String> artistTagList = null;
        try {
            URL url = AccessLastFM.getURL(method, artistTagCount);
            Document artistListXML = AccessLastFM.getResponse(url);
            NodeList artistTagInfo = AccessLastFM.getElementList(artistListXML, "tag");
            artistTagList = AccessLastFM.extractAttributes(artistTagInfo, "name");
        } catch (IOException | ParserConfigurationException | SAXException ex) {
            String msg = "Artist artistTagList unreachable or currupted";
            //LOGGER.log(Level.SEVERE, msg, ex);
        }
        return artistTagList;
    }

    private static void loadPreviousData() {
    }

    public static void buildDataSheet() {
        String dataSheetPath = ConfigParameters
                .configParameter().getParameter("dataSetFilePath");
        BufferedWriter tempWriter = getWriter(dataSheetPath);
        
        try {
            tempWriter.write("@relation dataSet");
            tempWriter.newLine();
            tempWriter.newLine();

            tempWriter.write("@attribute userID numeric");
            tempWriter.newLine();

            for (int index = 0; index < currentTagID - 1; index++) {
                tempWriter.write("@attribute tag" + index + " numeric");
                tempWriter.newLine();
            }

            tempWriter.newLine();
            tempWriter.write("@data");
            tempWriter.newLine();

            for (String userID : initialUsers.keySet()) {
                tempWriter.write(userID + initialUsers.
                        get(userID).getTasteString(currentTagID - 1));
                tempWriter.newLine();
            }
            tempWriter.close();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "File path for saving dataset is invalid", e);
        }
    }
    
    private static BufferedWriter getWriter(String filePath) {
        File tempFile = new File(filePath);
        BufferedWriter bufferedWriter = null;
        try {
            if (!tempFile.exists()) {
                tempFile.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(tempFile.getAbsoluteFile());
            bufferedWriter = new BufferedWriter(fileWriter);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "File path is invalid", e);
        }
        return bufferedWriter;
    }

    public static List<User> getInitialUserList() {
        List<User> userList = new ArrayList<>();
        for(String userID : initialUsers.keySet()){
            userList.add(initialUsers.get(userID));
        }
        return userList;
    }

    public static int getInitialTagCount() {
        return initialTags.size();
    }

    public static HashMap<String, Tag> getInitialTags() {
        return initialTags;
    }

}
