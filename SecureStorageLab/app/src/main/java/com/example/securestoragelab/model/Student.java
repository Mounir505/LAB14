    package com.example.securestoragelab.model;

    /**
     * Modèle de données immuable représentant un étudiant.
     */
    public class Student {
        public final int id;
        public final String name;
        public final int age;

        public Student(int id, String name, int age) {
            this.id = id;
            this.name = name;
            this.age = age;
        }
    }