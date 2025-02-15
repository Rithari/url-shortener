# URL Shortening Service
This project was done during a weekend to learn and freshen up previously used technologies. 

The stack used is:
* C# Console App for the user facing interface to interact with the API
* Java Spring Boot API which interacts with the Object Storage MongoDB to handle URL and User operations
* Redis to cache frequent request to prevent database overhead and shorten lookup times (not just links :) )


## System Design Diagram of the idea
![image](https://github.com/user-attachments/assets/3fcd278f-03b6-4bfd-b7a0-68c965242541)

Differentiating the current state of the project from the diagram:
- No Load Balancing
- No Multi-Instance API
- No Multi-Instance Cache
- The "Web Application" due to time constraints is a console app. Should be ASP.NET in the future.


## Further improvement ideas:
Many of the improvement ideas below could be thrown out and this project made into a personal shortening service, to keep locally or host on one's server.
Containerizing would make this extremely easy to deploy.

* Implement sharding in the MongoDB database
* Setup Docker and containerize the project
* Real authentication for the users
* Rate limiting & Load Balancing
* Multi Instance the API servers
* CD
