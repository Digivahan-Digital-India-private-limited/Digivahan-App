package com.digivahan.ui.Activities.chat;

import android.content.Context; // Used to access application resources and preferences

import com.digivahan.data.api.APIData; // Contains BASE_URL for socket server
import com.digivahan.data.local.PreferencesManager; // Used to get logged-in userId
import com.digivahan.utils.CommonLogic; // Your logging utility

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.IO; // Socket.IO client
import io.socket.client.Socket; // Socket instance
import io.socket.emitter.Emitter; // Listener for socket events

public class ChattingSystem {

    // Tag used for logging
    private static final String TAG = "ChattingSystem";

    // Socket object used for real-time communication
    private Socket socket;

    // Chat room ID where users will join
    private final String roomId;

    // Listener to send received messages to Activity
    private OnMessageReceivedListener messageListener;

    // Listener to inform Activity about connection state
    private final SocketConnectionListener connectionListener;

    // Used to get stored user information (userId)
    private PreferencesManager preferencesManager;

    /**
     * Interface used to send received messages to Activity
     */
    public interface OnMessageReceivedListener {
        void onMessageReceived(JSONObject messageResponse, boolean isSelf);
    }

    /**
     * Interface used to notify Activity about socket connection state
     */
    public interface SocketConnectionListener {
        void onConnected();      // called when socket connects
        void onDisconnected();   // called when socket disconnects
        void onError(String reason); // called when connection error occurs
    }

    /**
     * Constructor
     * Initializes socket and listeners
     */
    public ChattingSystem(Context context,
                          String roomId,
                          OnMessageReceivedListener messageListener,
                          SocketConnectionListener connectionListener) {

        this.roomId = roomId; // store room id
        this.messageListener = messageListener; // store message listener
        this.connectionListener = connectionListener; // store connection listener

        // initialize preference manager
        this.preferencesManager = new PreferencesManager(context);

        // start socket initialization
        initSocket();
    }

    /**
     * Initialize socket connection
     */
    private void initSocket() {

        // Prevent creating multiple socket instances
        if (socket != null) return;

        try {

            // Socket configuration options
            IO.Options options = new IO.Options();

            // enable auto reconnection
            options.reconnection = true;

            // try reconnecting 10 times
            options.reconnectionAttempts = 10;

            // wait 1 second before reconnect
            options.reconnectionDelay = 1000;

            // connection timeout
            options.timeout = 20000;

            // IMPORTANT
            options.transports = new String[]{"websocket"};

            // Create socket using server BASE_URL
            socket = IO.socket(APIData.BASE_URL, options);

            /**
             * EVENT: Socket Connected
             */
            socket.on(Socket.EVENT_CONNECT, args -> {

                CommonLogic.showTestLog(TAG, "✅ Socket Connected");

                try {

                    // Create JSON object for joining room
                    JSONObject joinObj = new JSONObject();

                    // room id to join
                    joinObj.put("roomId", roomId);

                    // current user id
                    joinObj.put("userId", preferencesManager.getUserId());

                    // emit event to server
                    socket.emit("join_room", joinObj);

                    CommonLogic.showTestLog(TAG, "📥 Joined room: " + roomId);

                } catch (JSONException e) {

                    // log error if JSON fails
                    CommonLogic.showTestLog(TAG, "Join room error: " + e.getMessage());
                }

                // notify activity that socket is connected
                if (connectionListener != null) {
                    connectionListener.onConnected();
                }
            });

            /**
             * EVENT: Socket Disconnected
             */
            socket.on(Socket.EVENT_DISCONNECT, args -> {

                CommonLogic.showTestLog(TAG, "⚠️ Socket Disconnected");

                // notify activity
                if (connectionListener != null) {
                    connectionListener.onDisconnected();
                }
            });

            /**
             * EVENT: Connection Error
             */
            socket.on(Socket.EVENT_CONNECT_ERROR, args -> {

                // get error message
                String error = args.length > 0 ? args[0].toString() : "Unknown error";

                CommonLogic.showTestLog(TAG, "❌ Socket Connect Error: " + error);

                // notify activity
                if (connectionListener != null) {
                    connectionListener.onError(error);
                }
            });

            /**
             * EVENT: Receive room members list
             */
            socket.on("room_members", args -> {

                // check if server sent data
                if (args.length > 0) {

                    // print members list
                    CommonLogic.showTestLog(TAG,"👥 Members: " + args[0].toString());
                }

            });

            /**
             * EVENT: Receive new message from server
             */
            socket.on("receive_message", onReceiveMessage);

            // finally connect socket
            socket.connect();

        } catch (Exception e) {

            // log initialization error
            CommonLogic.showTestLog(TAG, "Socket initialization failed: " + e.getMessage());
        }
    }

    /**
     * Listener for receiving messages
     */
    private final Emitter.Listener onReceiveMessage = args -> {

        try {

            // if server sent nothing, return
            if (args.length == 0) return;

            // get JSON message
            JSONObject data = (JSONObject) args[0];

            // log received message
            CommonLogic.showTestLog(TAG, "📩 Message received: " + data.toString());

            // get sender id from message
            String senderId = data.optString("from", "");

            // check if message is sent by current user
            boolean isSelf = senderId.equals(preferencesManager.getUserId());

            // send message to activity
            if (messageListener != null) {
                messageListener.onMessageReceived(data, isSelf);
            }

        } catch (Exception e) {

            // log parsing error
            CommonLogic.showTestLog(TAG, "❌ receive_message parse error: " + e.getMessage());
        }
    };

    /**
     * Check if socket is connected
     */
    public boolean isConnected() {
        return socket != null && socket.connected();
    }

    /**
     * Send message to server
     */
    public void sendMessage(String message) {

        // prevent empty message
        if (message == null || message.trim().isEmpty()) {

            CommonLogic.showTestLog(TAG, "⚠️ Empty message");
            return;
        }

        // check socket connection
        if (!isConnected()) {

            CommonLogic.showTestLog(TAG, "⚠️ Socket not connected");
            return;
        }

        try {

            // create JSON message
            JSONObject obj = new JSONObject();

            // room id
            obj.put("roomId", roomId);

            // message text
            obj.put("message", message);

            // empty images array
            obj.put("images", new org.json.JSONArray());

            // create location object
            JSONObject location = new JSONObject();

            location.put("latitude", "");
            location.put("longitude", "");

            obj.put("location", location);

            // send message event to server
            socket.emit("send_message", obj);

            // log emitted message
            CommonLogic.showTestLog(TAG, "📤 Message emitted: " + obj.toString());

        } catch (JSONException e) {

            // log error
            CommonLogic.showTestLog(TAG, "❌ sendMessage error: " + e.getMessage());
        }
    }

    /**
     * Disconnect socket safely
     */
    public void disconnect() {

        if (socket != null) {

            CommonLogic.showTestLog(TAG, "🔌 Disconnecting socket");

            // remove all listeners
            socket.off();

            // disconnect socket
            socket.disconnect();

            // close socket
            socket.close();

            // clear object
            socket = null;
        }
    }

    /*
    // use it to sat up shocket
    private void initializeChattingSystem(String roomId) {

        chattingSystem = new ChattingSystem(
                ChatActivity.this,
                roomId,
                (messageJson, isSelf) -> runOnUiThread(() -> {

                    ChatItemModel model = parseSocketMessage(messageJson);

                    if (isSelf) {

                        for (ChatItemModel item : chatItemList) {

                            if (item.isSending()) {

                                item.setSending(false);
                                item.setId(model.getId());

                                chatListAdapter.notifyDataSetChanged();

                                return;
                            }
                        }
                    }

                    if (!containsMessage(model.getId())) {

                        chatItemList.add(model);

                        chatListAdapter.notifyItemInserted(chatItemList.size() - 1);

                        boolean userAtBottom =
                                rvChatListLayoutManager.findLastVisibleItemPosition()
                                        >= chatListAdapter.getItemCount() - 1;

                        if (userAtBottom) {
                            binding.rvChatList.scrollToPosition(chatItemList.size() - 1);
                        } else {
                            newMessageCount++;
                        }
                    }

                }),
                new ChattingSystem.SocketConnectionListener() {

                    @Override
                    public void onConnected() {

                        runOnUiThread(() ->
                                CommonLogic.showTestLog(TAG, "🟢 Chat is LIVE"));

                    }

                    @Override
                    public void onDisconnected() {

                        runOnUiThread(() ->
                                CommonLogic.showTestLog(TAG, "🔴 Chat disconnected"));

                    }

                    @Override
                    public void onError(String reason) {

                        runOnUiThread(() ->
                                CommonLogic.showTestLog(TAG, "❌ Socket error: " + reason));

                    }
                }
        );
    }
    */

}