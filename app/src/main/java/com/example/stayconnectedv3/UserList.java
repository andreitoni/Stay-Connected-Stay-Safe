package com.example.stayconnectedv3;
/*
@author Andrei Toni Niculae
 */
import java.util.ArrayList;
import java.util.List;

public class UserList {

    List<User> users;

    public UserList() {
        users = new ArrayList<>();
    }
    // Check if the user list contains the user u.
    public boolean contains(User u) {
        for(User user : users) {
            if(u.getuID().equals(user.getuID())) {
                return true;
            }
        }
        return false;
    }
    // update the user's latitude and longitude in the users list.
    public void update(User u) {
        for(int i = 0; i < users.size(); i++) {
            if(users.get(i).equals(u)) {
                User temp = users.get(i);
                temp.update(u);
                users.set(i, temp);
            }
        }
    }

    public List<User> getUsers() {
        return users;
    }

    public void add(User u) {
        users.add(u);
    }

    public User get(int i) {
        return users.get(i);
    }
    public String toString() {
        return this.getUsers().toString();
    }
}
