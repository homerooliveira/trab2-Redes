package Server;

import java.io.IOException;

import java.lang.reflect.Type;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import Shared.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Server {
	static ArrayList<Room> rooms = new ArrayList<Room>();

	public Server() {
		System.out.println("Server is online.");
		lintenClients();
	}

	public void lintenClients() {
		DatagramSocket serverSocket;
		try {
			serverSocket = new DatagramSocket(9876);
			byte[] receiveData = new byte[1024];

			while (true) {
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				serverSocket.receive(receivePacket);
				String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
				Type type = new TypeToken<HashMap<String, Object>>(){}.getType();
				HashMap<String, String> request = new Gson().fromJson(receivedMessage, type);
				switch (request.get(Message.TYPE)) {
				case Message.AVAILIBLE_ROOMS:
					availibleRoomsRequest(receivedMessage);
					break;

				case Message.CREATE_ROOM:
					createRoomRequest(receivedMessage);
					break;
				case Message.JOIN_ROOM:
					joinRoomRequest(receivedMessage);
					break;
				case Message.PLAYER_ACTION:
					playerActionRequest(receivedMessage);
					break;
				default:
					break;
				}
				
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void availibleRoomsRequest(String message) {
		Type type = new TypeToken<AvailibleRoomsRequest>(){}.getType();
		AvailibleRoomsRequest availibleRoomsRequest = new Gson().fromJson(message, type);
		String clientIP = availibleRoomsRequest.from;

		AvailibleRoomsResponse availibleRoomsResponse = new AvailibleRoomsResponse(rooms);
		String responseMessage = new Gson().toJson(availibleRoomsResponse);
		sendMessageToClient(responseMessage, clientIP);
	}

	public void createRoomRequest(String message) {
		Type type = new TypeToken<CreateRoomRequest>(){}.getType();
		CreateRoomRequest createRoomRequest = new Gson().fromJson(message, type);
		String roomName = createRoomRequest.roomName;
		String clientIP = createRoomRequest.from;
		
		CreateRoomResponse createRoomResponse;
		Room newRoom = new Room(roomName, clientIP);
		if (!rooms.contains(newRoom)) {
			rooms.add(newRoom);
			createRoomResponse = new CreateRoomResponse(roomName, null, rooms, "");
		} else {
			createRoomResponse = new CreateRoomResponse(roomName, newRoom, rooms, "This room already exists!");
		}

		String responseMessage = new Gson().toJson(createRoomResponse);
		sendMessageToClient(responseMessage, clientIP);
	}

	public void joinRoomRequest(String message) {
		Type type = new TypeToken<JoinRoomRequest>(){}.getType();
		JoinRoomRequest joinRoomRequest = new Gson().fromJson(message, type);
		String roomName = joinRoomRequest.roomName;
		String clientRequestIP = joinRoomRequest.from;

		Room room = null;
//		for (Room r : rooms) {
//			if (r.name.equals(roomName)) {
//				r.secondPlayerIP = clientRequestIP;
//				room = r;
//				break;
//			}
//		}

		for (int roomIndex = 0; roomIndex <= rooms.size(); roomIndex++) {
			Room roomAux = rooms.get(roomIndex);
			
			if (roomAux.name.equals(roomName)) {
				roomAux.secondPlayerIP = clientRequestIP;
				room = roomAux;
				rooms.remove(roomIndex);
				break;
			}
		}

		String clientResponseIP = room.firstPlayerIP;

		RoomUpdateResponse roomUpdateResponse = new RoomUpdateResponse(room, "");
		String responseMessage = new Gson().toJson(roomUpdateResponse);
		sendMessageToClient(responseMessage, clientResponseIP);
	}

	public void playerActionRequest(String message) {
		Type type = new TypeToken<PlayerActionRequest>(){}.getType();
		PlayerActionRequest playerActionRequest = new Gson().fromJson(message, type);
		Room room = playerActionRequest.room;
		String fromUser = playerActionRequest.from;
		String destinationUser = null;
		if (fromUser.equals(room.firstPlayerIP)) {
			destinationUser = room.secondPlayerIP;
		} else {
			destinationUser = room.firstPlayerIP;
		}
		RoomUpdateResponse roomUpdateResponse = new RoomUpdateResponse(room, "");
		String responseMessage = new Gson().toJson(roomUpdateResponse);
		sendMessageToClient(responseMessage, destinationUser);
	}

	public void sendMessageToClient(String message, String clientIP) {
		try {
			int port = 5000;
			DatagramSocket clientSocket = new DatagramSocket();
			// Debug
			InetAddress IPAddress = InetAddress.getByName("localhost");
			if (clientIP.equals("1")) {
				port = 5001;
			}
			// Final
//			InetAddress IPAddress = InetAddress.getByName(clientIP);
			byte[] sendData = message.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
			clientSocket.send(sendPacket);
			clientSocket.close();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
