# DocumentStore


Data Structures course project (Spring 2020): 

* Developed an application that takes document files, compresses and stores them in memory. 
* Programmed to keep a dictionary lookup which maps words to all documents containing that word. 
* User can define a limit of how many documents can be stored in memory, if the limit is reached the least used document is stored on the disk in JSON format. 
* System tracks additions to the application chronologically. Able to undo any operation in the application.
* Includes a search engine which manages two-tier RAM and Disk storage using JSON serialization and deserialization
* Converts text to PDF and vice versa

The class intended for the user to interact with is DocumentStoreImpl.java, and is located in the DocumentStore/src/main/java/edu/yu/cs/com1320/project/stage5/impl folder
