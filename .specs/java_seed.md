I want to create a seed project for example for future projects maintaining the best practices, clean architecture and simplificity over everything else, things that are non-negociable lack of integration tests
and unit tests:

the project consists of:
- user endpoint that will publish to an user_topic
- order endpoint that will publish to an order topic
- user_order kafka stream (topology) that will unify these two events and publish to user_order topic
- listener that will listen this user_order topic and persist into a table user_order
- user_order controller to expose the information

The structure is there already I need to implement, for kafka stream I want to rely on TopologyTestDriver for better results, all avros are there.
The projects lacks of, contract first approach which we need to add and flyway to manage the database schema.
create a docker-compose.yaml to allow run this project locally
for kafka stream let's add spring-cloud for simplicity.

IntegrationTest class, this file should spin up three containers in parallel.
 - kafka
 - schema registry
 - postgres
 - flyway should be executed

In the end generate postman collection into .postman folder for manual testing of user, and a readme.md