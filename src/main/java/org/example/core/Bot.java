package org.example.core;

import io.github.cdimascio.dotenv.Dotenv;
import org.example.factory.CircleFactory;
import org.example.factory.HoleFactory;
import org.example.factory.RectangleFactory;
import org.example.factory.TriangleFactory;
import org.example.model.Room;
import org.example.model.Wall;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

public class Bot extends TelegramLongPollingBot {

    // List of users who started chatting with the bot
    private final Map<Long, UserSession> sessions = new HashMap<>();

    // This variable helps to hide our Telegram Token and Username
    private final Dotenv dotenv = Dotenv.load();

    @Override
    public String getBotUsername() {
        return dotenv.get("BOT_USERNAME");
    }

    @Override
    public String getBotToken() {
        return dotenv.get("BOT_TOKEN");
    }

    @Override
    public void onUpdateReceived(Update update) {
        var msg = update.getMessage();

        if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = msg.getChatId();
            String text = msg.getText();

            handleMessage(chatId, text);
        }
    }

    public void handleMessage(Long userId, String text) {
        UserSession session = sessions.getOrDefault(userId, new UserSession());
        sessions.putIfAbsent(userId, session);

        if ("/start".equals(text)) {
            sessions.remove(userId);
            UserSession userSession = new  UserSession();
            sessions.put(userId, userSession);

            startCommandReceived(userId);
            sendMessage(userId, "Let's start calculating!" + "\n" +
                    "\n" + "Firstly, please enter the amount of rooms you would paint: ");
            return;
        }
        Optional<Double>maybeValue = parseInput(text);
        if (maybeValue.isEmpty()){
            sendMessage(userId, "Please enter valid number!");
            return;
        }
        double value = maybeValue.get();

        if ("ASK_ROOM_QTY".equals(session.currentStep)) {
            session.roomQty = value;
            session.currentRoomIndex = 0;

            if (!isInputLegit(userId,session.roomQty,"Please enter positive number!",
                    "You entered 0 rooms. No paint is needed." + "\n" + "\n", true, true, true, session)) {
                return;
            }
            session.currentStep = "ASK_WALL_QTY";
            sendMessage(userId, "Room #" + (session.currentRoomIndex + 1) + "\n" +
                    "Enter the amount of walls you would paint: ");
        } else if ("ASK_WALL_QTY".equals(session.currentStep)) {
            session.wallQty = Double.parseDouble(text);
            session.currentWallIndex = 0;

            if (!isInputLegit(userId,session.wallQty,"Please enter positive number!",
                    "You entered 0 walls. No paint is needed." + "\n" + "\n", true, true, true, session)) {
                return;
            }

            session.currentStep = "ASK_HEIGHT";
            sendMessage(userId, "Wall #" + (session.currentWallIndex + 1) + "\n" +
                    "Enter the height of the wall: ");

        } else if ("ASK_HEIGHT".equals(session.currentStep)) {
            session.height = Double.parseDouble(text);

            if (!isInputLegit(userId,session.height,"Please enter positive number!",
                    "Height must be greater than 0!", false, false, false, session)) {
                return;
            }
            session.currentStep = "ASK_LENGTH";
            sendMessage(userId, "Enter the length of the wall: ");

        } else if ("ASK_LENGTH".equals(session.currentStep)) {
            session.length = Double.parseDouble(text);

            if (!isInputLegit(userId,session.length,"Please enter positive number!",
                    "Length must be greater than 0!", false, false, false, session)) {
                return;
            }
            session.currentStep = "ASK_LAYERS";
            sendMessage(userId, "Enter the number of layers you will apply to the wall: ");

        } else if ("ASK_LAYERS".equals(session.currentStep)) {
            session.layers = Double.parseDouble(text);

            if (!isInputLegit(userId,session.layers,"Please enter positive number!",
                    "Layers must be greater than 0!", false, false, false, session)) {
                return;
            }
            session.currentStep = "ASK_CONSUMPTION";
            sendMessage(userId, "Enter the paint consumption(it is written on the back of the can): ");

        } else if ("ASK_CONSUMPTION".equals(session.currentStep)) {
            session.consumption = Double.parseDouble(text);

            if (!isInputLegit(userId,session.consumption,"Please enter positive number!",
                    "Consumption must be greater than 0!", false, false, false, session)) {
                return;
            }
            session.currentStep = "ASK_HOLE_QTY";
            sendMessage(userId, "Enter the number of holes on the wall: ");
        } else if ("ASK_HOLE_QTY".equals(session.currentStep)) {
            session.holeQty = Double.parseDouble(text);

            if (session.holeQty < 0 || session.holeQty % 1 != 0) {
                sendMessage(userId, "Please enter a whole positive number!");
                return;
            }
            session.currentHoleIndex = 0;

            if (session.holeQty == 0) {
                sendMessage(userId, "You entered 0 holes. Continuing wall calculation...");
                finishWall(session, userId);
            } else {
                session.currentStep = "ASK_HOLE_TYPE";
                sendMessage(userId, "Hole #" + (session.currentHoleIndex + 1) + "\n" +
                        "Enter the type: ('1' if circle / '2' if triangle / '3' if rectangle)");
            }
        } else if ("ASK_HOLE_TYPE".equals(session.currentStep)) {
            if (text.equalsIgnoreCase("1")) {
                session.currentStep = "ASK_CIRCLE_RADIUS";
                sendMessage(userId, "Enter the radius of the circle: ");
            } else if (text.equalsIgnoreCase("2")) {
                session.currentStep = "ASK_TRIANGLE_BASE";
                sendMessage(userId, "Enter the base of the triangle: ");
            } else if (text.equalsIgnoreCase("3")) {
                session.currentStep = "ASK_RECTANGLE_HEIGHT";
                sendMessage(userId, "Enter the height of the rectangle: ");
            } else {
                sendMessage(userId, "Unknown type of the hole! Enter:" +
                        " '1' if circle / '2' if triangle / '3' if rectangle");
            }

        }
        // CIRCLE DESCRIPTION
        else if ("ASK_CIRCLE_RADIUS".equals(session.currentStep)) {
            Optional<Double> maybeRadius = parseInput(text);
            if (maybeRadius.isEmpty()) {
                sendMessage(userId, "Please enter valid number!");
                return;
            }

            double circleRadius = maybeRadius.get();

            if (!isInputLegit(userId, circleRadius,
                    "Please enter positive number!",
                    "Radius must be greater than 0!", false, false, false, session)) {
                return;
            }
            HoleFactory factory = new CircleFactory(circleRadius);
            session.holes.add(factory.createHole());

            finishHole(session, userId);
        }
        // TRIANGLE DESCRIPTION
        else if ("ASK_TRIANGLE_BASE".equals(session.currentStep)) {
            session.tempParam1 = Double.parseDouble(text);

            if (!isInputLegit(userId,session.tempParam1,"Please enter positive number!",
                    "Base must be greater than 0!", false, false, false, session)) {
                return;
            }
            session.currentStep = "ASK_TRIANGLE_HEIGHT";
            sendMessage(userId, "Enter the height of the triangle: ");

        } else if ("ASK_TRIANGLE_HEIGHT".equals(session.currentStep)) {
            session.tempParam2 = Double.parseDouble(text);

            if (!isInputLegit(userId,session.tempParam2,"Please enter positive number!",
                    "Height must be greater than 0!", false, false, false, session)) {
                return;
            }

            HoleFactory factory = new TriangleFactory(session.tempParam1, session.tempParam2);
            session.holes.add(factory.createHole());

            finishHole(session, userId);
        }
        // RECTANGLE DESCRIPTION
        else if ("ASK_RECTANGLE_HEIGHT".equals(session.currentStep)) {
            session.tempParam1 = Double.parseDouble(text);

            if (!isInputLegit(userId,session.tempParam1,"Please enter positive number!",
                    "Height must be greater than 0!", false, false, false, session)) {
                return;
            }

            session.currentStep = "ASK_RECTANGLE_LENGTH";
            sendMessage(userId, "Enter the length of the rectangle: ");
        } else if ("ASK_RECTANGLE_LENGTH".equals(session.currentStep)) {
            session.tempParam2 = Double.parseDouble(text);

            if (!isInputLegit(userId,session.tempParam2,"Please enter positive number!",
                    "Length must be greater than 0!", false, false, false, session)) {
                return;
            }

            HoleFactory factory = new RectangleFactory(session.tempParam1, session.tempParam2);
            session.holes.add(factory.createHole());

            finishHole(session, userId);
        }
    }

    public void sendMessage(Long who, String what) {
        SendMessage sm = SendMessage.builder()
                .chatId(who.toString())             // Who are we sending a message to
                .text(what).build();                // Message content
        try {
            execute(sm);                            // Sending the message
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);          // Print error if caught
        }
    }

    public void startCommandReceived(Long userId) {
        String greeting = "Welcome to Paint Calculator bot!" + "\n" +
                "\n" + "This bot is for help with calculating paint for th" +
                "\n" + "e house, use it in good health!" + "\n";

        sendMessage(userId, greeting);
    }

    public void finishHole(UserSession session, Long userId) {
        session.currentHoleIndex++;
        if (session.currentHoleIndex < session.holeQty) {
            session.currentStep = "ASK_HOLE_TYPE";
            sendMessage(userId, "Enter the type of the #" + (session.currentHoleIndex + 1) +
                    " hole" + " ('1' if circle / '2' if triangle / '3' if rectangle):");
        } else
            finishWall(session, userId);
    }

    public void finishWall(UserSession session, Long userId) {
        Wall wall = session.buildWall();
        session.currentRoomWalls.add(wall);
        session.currentWallIndex++;

        if (session.currentWallIndex < session.wallQty) {
            session.currentStep = "ASK_HEIGHT";
            sendMessage(userId, "Wall #" + (session.currentWallIndex + 1) + "\n" +
                    "Enter the height of the wall:");
        } else
            finishRoom(session, userId);

        // Clear data
        session.holes.clear();
        session.holeQty = 0;
        session.currentHoleIndex = 0;
    }

    public void finishRoom(UserSession session, Long userId) {
        Room room = new Room(new ArrayList<>(session.currentRoomWalls));
        session.rooms.add(room);

        session.currentRoomIndex++;
        if (session.currentRoomIndex < session.roomQty) {
            session.currentWallIndex = 0;
            session.currentRoomWalls.clear();
            session.currentStep = "ASK_WALL_QTY";
            sendMessage(userId, "Room #" + (session.currentRoomIndex + 1)
                    + "\n" + "Enter the amount of walls you would paint: ");
        } else {
            double totalPaint = 0;
            for (Room rooms : session.rooms) {
                totalPaint += rooms.getTotalPaint();
            }
            sendMessage(userId, "Total paint needed: " + Math.ceil(totalPaint) + " liters." +
                    "\n" + "\n" + " use " + "'/start' again, if you want to :).");
        }
    }

    public boolean isInputLegit(Long userId, double value,
                                String negativeMsg, String zeroMsg,
                                boolean removeSession, boolean ifRoomIsZero,
                                boolean mustBeInteger, UserSession session) {
        if (value < 0){
            sendMessage(userId, negativeMsg);
            if (removeSession) {
                sessions.remove(userId);
            }
            return false;
        }
        if (value == 0){
            sendMessage(userId, zeroMsg);
            if (removeSession) {
                sessions.remove(userId);
            }
            if(ifRoomIsZero){
                finishRoom(session, userId);
            }
            return false;
        }
        if (mustBeInteger && value % 1 != 0){
            sendMessage(userId, "Please enter a whole number!");
            return false;
        }
        return true;
    }
    public Optional<Double> parseInput(String text) {
        try {
            double value = Double.parseDouble(text);
            return Optional.of(value);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}

