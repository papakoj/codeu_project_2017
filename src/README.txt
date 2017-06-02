I have implemented persistent storage for users using SQLite. 
I will be attempting to expand this to include messages and conversations, 
however I'm not entirely sure how feasible this goal is in the remaining time.

TODO

Create tables for messages and conversations.
Add code to update the tables when new messages/conversations are added.
Add code to populate the client with stored messages/conversation on startup.

Implement the following methods to aid in enabling storage of messages and conversations 
getMessagesForConversation(ConvId conv): List<Messages>
// Return all messages for conversation id
getConversationsForUser(UUID user): List<UUID>
// Return all conversation ids for a user id

