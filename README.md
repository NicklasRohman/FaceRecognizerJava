# FaceRecognizerJava

## TECHNOLOGY USED
* Core Java
* JavaCV (wrapper of Opencv )
* JavaFX
* MySQL
* Maven

### IDE
I have developed this project using Eclipse Neon.

### Required Software
To properly run this Application on your System, At first you need to download and install the following software:

 * An implementation of **Java SE** 8 or newer:
   * **JDK**  http://www.oracle.com/technetwork/java/javase/downloads/index.html
 
 * An implementation of **JavaFX**: (Follow The Installation Instruction)
   * **JavaFX**  https://docs.oracle.com/javafx/2/installation/jfxpub-installation.htm

 * An implementation of **JavaCV**: (Follow The Installation Instruction)
   * **JavaCV**  https://github.com/bytedeco/javacv
   
 * An implementation of **MySQL Database**: 
   * **XAMPP**  https://www.apachefriends.org/download.html
 
 * To Connect MySQL with Java, You will need a connector: (Follow the installation instruction)
   * **MySQL Connector Java**  https://dev.mysql.com/downloads/connector/j/5.1.html
  
 * An implementation of **JavaFX Scene Builder**: (Follow The Installation Instruction)
   * **JavaFX Scene Builder**  https://docs.oracle.com/javafx/scenebuilder/1/installation_1-0/jsbpub-installation_1-0.htm

Make sure everything has the same bitness: **32-bit and 64-bit modules. I used 64-bit.

## Maven Dependencies
<dependency>
	<groupId>mysql</groupId>
	<artifactId>mysql-connector-java</artifactId>
	<version>5.1.14</version>
</dependency>
<dependency>
	<groupId>org.bytedeco.javacpp-presets</groupId>
	<artifactId>tesseract-platform</artifactId>
	<version>3.05.01-1.4</version>
</dependency>
<dependency>
	<groupId>org.bytedeco</groupId>
	<artifactId>javacv-platform</artifactId>
	<version>1.4</version>
</dependency>
<dependency>
	<groupId>org.bytedeco.javacpp-presets</groupId>
	<artifactId>opencv</artifactId>
	<version>3.4.0-1.4</version>
</dependency>
<dependency>
	<groupId>org.bytedeco.javacpp-presets</groupId>
	<artifactId>ffmpeg</artifactId>
	<version>3.4.1-1.4</version>
</dependency>
# Configuration Settings 
#### Before executing the App, you have to make some change to below mentioned files...

## Database Settings 
* Open MySQL on XAMPP then Create a New Database & name it **face** 
* Now import attached **face_bio.sql** to the  **face** Database
* Or Create a New Schema(Table)

SQL code:
CREATE TABLE IF NOT EXISTS `face_bio` (
`id` int(11) NOT NULL,
  `code` int(10) NOT NULL,
  `first_name` varchar(30) NOT NULL,
  `last_name` varchar(20) NOT NULL,
  `reg` int(10) NOT NULL,
  `age` int(10) NOT NULL,
  `section` varchar(20) NOT NULL
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;


ALTER TABLE `face_bio`
 ADD PRIMARY KEY (`id`);
 
 ALTER TABLE `face_bio`
MODIFY `id` int(11) NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=1;

* Now Open src/application/Database.java and provide your MySQL DB credintials
package application;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

class Database {
	public int code;

	public String fname;
	public String Lname;
	public int reg;
	public int age;
	public String sec;

	public final String Database_name = "Your Database";
	public final String Database_user = "Your Database user Name";
	public final String Database_pass = "Your Database Password";

# Tips
* When you going to train a new face try to capture at least 10 pictures of a single person in different angle.keep it in mind that the more number of training image in different angle and posture, the more accurate it will be face recognition.

# Please Note:
Please Keep it in mind ,sometimes the face recognition algorithm provides wrong output if a person is not trained or unknown to the system.The reason behind this is that the face recognition algorithm guess the face with the nearest match.when it does not find any match ,the system pick any face with a nearest match.this is  why it sometimes provides wrong output.
