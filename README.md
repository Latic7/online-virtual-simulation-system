### STILL IN ACTIVE DEVELOPMENT, THIS VERSION IS UNSTABLE
### Description
An online virtual simulation system for viewing 3D models in real-time. Based on Spring Boot and Three.js. 
This is part of a URP project, i.e. Undergraduate Research Programme, of China Agricultural University. 
### How to use
1) Use *git clone* to clone this project into your destined directory.
2) Open the folder as a project with IntelliJ Idea Ultimate. *OnlineVirtualSimulationSystemApplication.java* should be set as the programme's entry point. 
3) You will also need an SQL database named *ovss_db* with four tables inside. What these tables should contain can be inferred through the entity classes inside the project. *Or* maybe someday I will export this database through SQL script and include it here, inside this fork, so that everyone can easily create their own required database. But for now this project is still in active development, I cannot guarantee that no changes will take place in the future, so I shall not include the database here unless everything goes stable. 
4) A database user named *ovss_root* with password *123456* is also required. This user should have full access to database *ovss_db* for everything to work. 
