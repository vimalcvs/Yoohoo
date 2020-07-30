package com.verbosetech.yoohoo.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;

/**
 * Created by a_man on 30-12-2017.
 */
public class Group implements Parcelable {
    private String id;
    private String name, status, image;
    private ArrayList<String> userIds;
    @Exclude
    private ArrayList<User> users;

    public Group() {
    }

    protected Group(Parcel in) {
        id = in.readString();
        name = in.readString();
        status = in.readString();
        image = in.readString();
        userIds = in.createStringArrayList();
        users = in.createTypedArrayList(User.CREATOR);
    }

    public static final Creator<Group> CREATOR = new Creator<Group>() {
        @Override
        public Group createFromParcel(Parcel in) {
            return new Group(in);
        }

        @Override
        public Group[] newArray(int size) {
            return new Group[size];
        }
    };

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public ArrayList<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(ArrayList<String> userIds) {
        this.userIds = userIds;
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(status);
        dest.writeString(image);
        dest.writeStringList(userIds);
        dest.writeTypedList(users);
    }
}
