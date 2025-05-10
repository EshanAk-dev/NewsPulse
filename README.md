# 📰 News Reporting Mobile App

A powerful **news reporting mobile application** built with **Kotlin** and backed by **Firebase**, featuring a **role-based system** for Admins, Reporters, Editors, and general Users. This app facilitates real-time news submission, editorial review, and publication for public viewing.

---

## 📱 Overview

This mobile application enables structured news management in a dynamic workflow:

* **Reporters** submit raw news articles
* **Editors** review, edit, approve, or delete news
* **Admins** manage users and roles
* **General Users** read published news

Built for **Android** using modern **Jetpack components** and **Firebase services** like Firestore, Authentication, and Cloud Storage.

---

## 🛠️ Tech Stack

* **Language**: Kotlin (Android)
* **Backend**: Firebase

  * Firestore (Realtime NoSQL database)
  * Firebase Authentication
  * Firebase Storage (for image uploads)

---

## 👥 User Roles & Permissions

| Role         | Description                                        |
| ------------ | -------------------------------------------------- |
| **Admin**    | Manage user accounts and assign roles              |
| **Reporter** | Submit and update news drafts with optional images |
| **Editor**   | Edit, approve, publish, or delete submitted news   |
| **User**     | View and read only published news articles         |

---

## 📦 Features

### ✅ General

* Firebase Auth login (email/password)
* Realtime database updates with Firestore
* Role-based access control (RBAC)

### 📝 Reporter

* Submit news (title, content, image)
* Edit or delete unapproved drafts

### 🧾 Editor

* Review submitted news
* Edit content and media
* Publish or reject news

### 🛠️ Admin

* View list of registered users
* Assign and manage roles
* Delete user accounts

### 👀 User

* View only published news
* Search and filter by category or date
