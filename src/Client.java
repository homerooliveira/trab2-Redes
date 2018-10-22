package Client;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

import Shared.AvailibleRoomsRequest;
import Shared.AvailibleRoomsResponse;
import Shared.CreateRoomRequest;
import Shared.CreateRoomResponse;
import Shared.JoinRoomRequest;
import Shared.PlayerActionRequest;
import Shared.Room;
import Shared.RoomUpdateResponse;
import Shared.Util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Client {

	String myIP = null;
	String serverIP = "192.168.0.1";

	Room currentRoom;
	ArrayList<Room> rooms = new ArrayList<Room>();

	boolean isFirstPlayer;
	Scanner input = new Scanner(System.in);

	public Client() {
		start();
	}

	public void start() {
		System.out.println("--- Welcome ---");
		System.out.println("Please, enter your IP:");
		myIP = input.next();
		boolean exit = false;
		while (!exit) {
			ArrayList<Room> availibleRooms = availibleRoomsRequest();
			rooms = availibleRooms;
			showAvailibleRooms();
			System.out.println("--- Options ---");
			System.out.println("0 - Create room");
			if (rooms.size() > 0) {
				System.out.println("1 - Join room");
			}
			System.out.println("2 - Refresh");
			System.out.println("3 - Exit");
			System.out.println("Enter an option:");
			String userInput = input.next();
			switch (userInput) {
			case "0":
				createRoom();
				break;

			case "1":
				if (rooms.size() > 0) {
					joinRoom();
				} else {
					invalidOption();
				}
				break;

			case "2":
				continue;
			case "3":
				System.out.println("Closing application...");
				exit = true;
				break;

			default:
				Util.clearConsole();
				System.out.println("Invalid option");
				break;
			}
		}
	}

	public void showAvailibleRooms() {
		if (rooms.size() > 0) {
			System.out.println("--- Availible rooms ---");
			for (int roomNumber = 0; roomNumber<rooms.size(); roomNumber++) {
				System.out.println(roomNumber + " - " + rooms.get(roomNumber));
			}
		} else {
			System.out.println("--- No rooms avilible ---");
		}
	}
	
	public void createRoom() {
		System.out.println("Enter the name of the room to be created:");
		String roomName = input.next().trim();
		if (!roomName.isEmpty()) {
			System.out.println("Please wait...");
			CreateRoomResponse createRoomResponse = createRoomRequest(roomName);
			if (!createRoomResponse.errorMessage.isEmpty()) {
				Util.clearConsole();
				System.out.println(createRoomResponse.errorMessage);
			} else {
				isFirstPlayer = true;
				currentRoom = createRoomResponse.room;
				System.out.println("Room created successfully!");
				System.out.println("Waiting some user join the room...");
				RoomUpdateResponse roomUpdateResponse = roomUpdateResponse();
				if (!roomUpdateResponse.errorMessage.isEmpty()) {
					System.out.println(roomUpdateResponse.errorMessage);
				} else {
					currentRoom = roomUpdateResponse.room;
					System.out.println("The game has started, you`re the symbol X");
					play();
				}
			}
		} else {
			Util.clearConsole();
			System.out.println("The room`s name can`t be empty!");
		}
	}

	public void joinRoom() {
		Util.clearConsole();
		showAvailibleRooms();
		System.out.println("Enter the room`s number:");
		String roomSelected = input.next();
		try {
			int roomSelectedNumber = Integer.parseInt(roomSelected);
			if (roomSelectedNumber >= 0 && roomSelectedNumber < rooms.size()) {
				System.out.println("Please wait...");
				isFirstPlayer = false;
				RoomUpdateResponse roomUpdateResponse =  joinRoomRequest(rooms.get(roomSelectedNumber).name);
				if (!roomUpdateResponse.errorMessage.isEmpty()) {
					Util.clearConsole();
					System.out.println(roomUpdateResponse.errorMessage);
				} else {
					currentRoom = roomUpdateResponse.room;
					System.out.println("The game has started, you`re the symbol Y");
					play();
				}
			} else {
				invalidOption();
			}
		} catch (Exception e) {
			invalidOption();
		}
	}

	public void invalidOption() {
		Util.clearConsole();
		System.out.println("Invalid option");
	}

	public void matchTied() {
		Util.clearConsole();
		System.out.println("The match tied!");
		currentRoom.board.print();
	}

	public boolean isValidMove(String move) {
		boolean isValid = false;
		String[] points = move.split(",");
		if (points.length != 2) {
			isValid = false;
		}
		try {
			int x = Integer.parseInt(points[0]);
			int y = Integer.parseInt(points[1]);
			if (currentRoom.board.isPositionValid(x, y)) {
				isValid = true;
			}
		} catch (Exception e) {
			isValid = false;
		}
		return isValid;
	}

	public void doMove(String move) {
		String[] points = move.split(",");
		int x = Integer.parseInt(points[0]);
		int y = Integer.parseInt(points[1]);
		if (isFirstPlayer) {
			currentRoom.board.markFirstPlayerPosition(x, y);
		} else {
			currentRoom.board.markSecondPlayerPosition(x, y);
		}
	}

	public void play() {
		System.out.println("It`s your time.");
		currentRoom.board.print();
		System.out.println("Enter the position (x,y) you want to make your move:");
		String move = input.next();
		while (!isValidMove(move)) {
			System.out.println("Invalid move!");
			System.out.println("Please enter a valid move:");
			move = input.next();
		}
		doMove(move);
		currentRoom.board.print();
		PlayerActionRequest playerActionRequest = new PlayerActionRequest(currentRoom, myIP);
		String requestMessage = new Gson().toJson(playerActionRequest);
		sendMessageToServer(requestMessage);
		if ((isFirstPlayer && currentRoom.board.verifyFirstPlayerMatch()) || (!isFirstPlayer && currentRoom.board.verifySecondPlayerMatch())) {
			System.out.println("Congratulations you won!");
			currentRoom.board.print();
		} else if(currentRoom.board.isFull()) {
			matchTied();
		} else {
			System.out.println("Waiting action from the other side...");
			RoomUpdateResponse roomUpdateResponse = roomUpdateResponse();
			Util.clearConsole();
			if (!roomUpdateResponse.errorMessage.isEmpty()) {
				System.out.println(roomUpdateResponse.errorMessage);
			} else {
				currentRoom = roomUpdateResponse.room;	
				if ((isFirstPlayer && currentRoom.board.verifySecondPlayerMatch()) || (!isFirstPlayer && currentRoom.board.verifyFirstPlayerMatch())) {
					System.out.println("Sorry, you lose!");
					currentRoom.board.print();
				} else if (currentRoom.board.isFull()) {
					matchTied();
				} else {
					play();
				}
			}
		}
	}

	public RoomUpdateResponse joinRoomRequest(String roomName) {
		JoinRoomRequest joinRoomRequest = new JoinRoomRequest(roomName, myIP);
		String messageRequest = new Gson().toJson(joinRoomRequest);
		sendMessageToServer(messageRequest);

		String serverResponse = lintenServer();
		Type type = new TypeToken<RoomUpdateResponse>(){}.getType();
		return new Gson().fromJson(serverResponse, type);
	}

	public CreateRoomResponse createRoomRequest(String roomName) {
		CreateRoomRequest createRoomRequest = new CreateRoomRequest(roomName, myIP);
		String requestJson = new Gson().toJson(createRoomRequest);
		sendMessageToServer(requestJson);

		String serverResponse = lintenServer();
		Type type = new TypeToken<CreateRoomResponse>(){}.getType();
		CreateRoomResponse createRoomResponse = new Gson().fromJson(serverResponse, type);
		return createRoomResponse;
	}

	public RoomUpdateResponse roomUpdateResponse() {
		String responseMessage = lintenServer();
		Type type = new TypeToken<RoomUpdateResponse>(){}.getType();
		RoomUpdateResponse roomUpdateResponse = new Gson().fromJson(responseMessage, type);
		return roomUpdateResponse;
	}

	public ArrayList<Room> availibleRoomsRequest() {
		AvailibleRoomsRequest availibleRoomsRequest = new AvailibleRoomsRequest(myIP);
		String  requestJson = new Gson().toJson(availibleRoomsRequest);
		sendMessageToServer(requestJson);

		String serverResponse = lintenServer();
		Type type = new TypeToken<AvailibleRoomsResponse>(){}.getType();
		AvailibleRoomsResponse availibleRoomsResponse = new Gson().fromJson(serverResponse, type);
		return availibleRoomsResponse.rooms;
	}

	public void sendMessageToServer(String message) {
		try {
			// Debug
			DatagramSocket clientSocket = new DatagramSocket();
			InetAddress IPAddress = InetAddress.getByName("localhost");
			// Final
//			InetAddress IPAddress = InetAddress.getByName("10.132.252.41");
			byte[] sendData = message.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
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

	public String lintenServer() {
		try {
			int port = 5000;
			// Debug
			if (myIP.equals("1")) {
				port = 5001;
			}
			DatagramSocket serverSocket = new DatagramSocket(port);
			byte[] receiveData = new byte[1024];

			while (true) {
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				serverSocket.receive(receivePacket);
				// message received from server
				String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
				serverSocket.close();
				return receivedMessage;
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}

//HashMap<String, String> request = new HashMap<String, String>();  
//request.put(RequestResponse.REQUEST_TYPE, RequestResponse.CREATE_ROOM);
//request.put(RequestResponse.FROM, ip);
//request.put(RequestResponse.ROOM_NAME, roomName);