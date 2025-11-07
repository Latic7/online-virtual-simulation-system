### STILL IN ACTIVE DEVELOPMENT, THIS VERSION IS UNSTABLE
### Description
An online virtual simulation system for viewing 3D models in real-time. Based on Spring Boot and Three.js. 
This is part of a URP project, i.e. Undergraduate Research Programme, of China Agricultural University. 
### How to use
1) Use *git clone* to clone this project into your destined directory.
2) Open the folder as a project with IntelliJ Idea Ultimate. *OnlineVirtualSimulationSystemApplication.java* should be set as the programme's entry point. 
3) You will also need an SQL database named *ovss_db* with additional tables inside. The database can be easily created by running the *create_ovss_db.sql* SQL script file included under /src/main/reasources. **WARNING**: This SQL script file **CONTAINS DELETION STATEMENTS** and may accidentally delete pre-existing databases and tables that have similar names. Please double-check before running this SQL script file. 
4) A database user named *ovss_root* with password *123456* is also required. This user should have full access to database *ovss_db* for everything to work. 
