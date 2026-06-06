package com.example.securestoragelab.files;

import android.content.Context;
import com.example.securestoragelab.model.Student;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public final class StudentsJsonStore {

    public static final String FILE_NAME = "students.json";

    private StudentsJsonStore() {}

    public static void save(Context context, List<Student> students) throws Exception {
        String json = toJson(students);
        InternalTextStore.writeUtf8(context, FILE_NAME, json);
    }

    public static List<Student> load(Context context) {
        try {
            String json = InternalTextStore.readUtf8(context, FILE_NAME);
            return fromJson(json);
        } catch (Exception e) {
            // Si le fichier est absent ou corrompu, on retourne une liste vide
            return new ArrayList<>();
        }
    }

    public static boolean delete(Context context) {
        return InternalTextStore.delete(context, FILE_NAME);
    }

    private static String toJson(List<Student> students) throws Exception {
        JSONArray arr = new JSONArray();
        for (Student s : students) {
            JSONObject obj = new JSONObject();
            obj.put("id", s.id);
            obj.put("name", s.name);
            obj.put("age", s.age);
            arr.put(obj);
        }
        return arr.toString();
    }

    private static List<Student> fromJson(String json) throws Exception {
        JSONArray arr = new JSONArray(json);
        List<Student> list = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            list.add(new Student(obj.getInt("id"), obj.getString("name"), obj.getInt("age")));
        }
        return list;
    }
}
