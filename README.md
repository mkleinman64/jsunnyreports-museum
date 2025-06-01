# jSunnyReports – The Museum Edition™

> “Still works. No tests. No shame.”

---

## ⚠️ Warning: This is not modern software.

This is vintage.  
This is handcrafted pre-Spring XML-free, annotationless Java.  
This is *I-used-JDeveloper-and-I-liked-it* Java.  

---

## 🏺 What is this?

**jSunnyReports** is a solar inverter log processor and HTML report generator written when:
- Java 1.5 was still a thing,
- SVN ruled the world,

It was built out of pure frustration with Excel, commercial solar logging software, and whatever SMA shipped with their inverters in 2010. And moreover because I had a SMA inverter *AND* a OK4E inverter and I could not combine the two!

So hence JSunnyreports was born and.. well.. like something I do.. It got out of hand completely!

It still works. Somehow.

---

## ⚙️ Features

- Processes inverter logs from multiple brands (SMA, Goodwe, Solax, random XML dumps, your mom's USB stick).
- Outputs static HTML reports with fancy tables and nostalgic design.
- Generates various json files for dynamic frontend rendering via jQuery (yes, seriously).
- Backwards compatible with Java 1.6 ( Maybe 1.5 ). 
- Also compiles under Java 8. Even Java 23! It compiles at least under Java 23. If it works. No idea.

---

## 🧙‍♂️ Requirements

- Java 8+
- Maven (but only after you kick its compiler plugin in the face and update the version)
- Strong emotional support when looking at the code
- Preferably not JDeveloper anymore (we moved on… kind of)

---

## 💀 How to build

```bash
git clone https://github.com/mkleinman64/jsunnyreports-museum.git
cd jsunnyreports-museum
mvn clean install
Download the original JSR Zip file from https://jsunnyreports.com ( 2.7.x ).
Copy your newly created target/JSunnyReports.jar to your dir.
Run JSR!

And ehhm. problaby pray that it works :P
