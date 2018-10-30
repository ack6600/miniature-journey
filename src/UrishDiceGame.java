import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;

public class UrishDiceGame {
    private enum LowerCats {
        ThreeOfAKind,
        FourOfAKind,
        FullHouse,
        SmallStraight,
        LargeStraight,
        YAHTZEE,
        Chance
    }

    public static final int MAX_PLAYERS = 6;
    public static final int DICE_SIDES = 6;

    Scanner sc;
    private ArrayList<Player> players;
    private Dice[] dice;

    public static void main(String[] args) {
        Scanner tempScan = new Scanner(System.in);
        boolean fast = false, debug = false, roll = true;
        System.out.printf("Welcome to Yahtzee! How many players? (Max %d)\n", MAX_PLAYERS);
        String input = tempScan.nextLine();
        int players;
        try{
            players = Integer.parseInt(input);
        }catch (NumberFormatException e){
            players = -1;
        }
        while(!(players > 0 && players < MAX_PLAYERS + 1)){
            if(input.equalsIgnoreCase("fast")) {
                fast = true;
                System.out.println(";)");
            }
            if(input.equalsIgnoreCase("debug")) {
                debug = true;
                System.out.println(";)");
            }
            if(input.equalsIgnoreCase("noroll")) {
                roll = false;
                System.out.println(";)");
            }
            if(input.equalsIgnoreCase("all")){
                fast = true;
                debug = true;
                roll = false;
                players = 6;
                System.out.println(";)");
                break;
            }
            System.out.println("Whoops! Invalid number of players. Try again.");
            input = tempScan.nextLine();
            try{
                players = Integer.parseInt(input);
            }catch (NumberFormatException e){
                players = -1;
            }
        }
        System.out.printf("Starting game with %d players\n", players);
//        tempScan.close();
        UrishDiceGame mainGame = new UrishDiceGame(players);
        mainGame.start(roll,fast,debug);
    }

    public UrishDiceGame(int players){
        sc = new Scanner(System.in);
        this.players = new ArrayList<>(players);
        for(int i = 0; i < players; i++)
            this.players.add(i, new Player(i + 1));
        dice = new Dice[]{new Dice(DICE_SIDES),
                new Dice(DICE_SIDES),
                new Dice(DICE_SIDES),
                new Dice(DICE_SIDES),
                new Dice(DICE_SIDES)};
    }

    public void start(boolean rollForStart, boolean fast, boolean debug){
        int firstPlayerIndex = 0;
        if(rollForStart){
            int[] rolls = new int[players.size()];
            for(Player player : players){
                int total = 0;
                System.out.printf("Player %d rolling...\n", player.getNum());
                try {
                    if(!fast)
                        Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for(int i = 0; i < dice.length; i++) {
                    System.out.printf("\tRoll %d, rolled a %d\n", i+1, dice[i].getValue(true));
                    try {
                        if(!fast)
                            Thread.sleep(250);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    total += dice[i].getValue(false);
                }
                System.out.printf("\tTotal roll: %d\n", total);
                rolls[player.num - 1] = total;
                try {
                    if(!fast)
                        Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            int biggest = 0;
            for(int i = 0; i < rolls.length; i++){
                if(rolls[i] > biggest) {
                    biggest = rolls[i];
                    firstPlayerIndex = i;
                }
            }
        }
        System.out.printf(debug ? "Player %d goes first. (index = %d)\n" : "Player %d goes first.\n", firstPlayerIndex + 1, firstPlayerIndex);
        runGameLoop(firstPlayerIndex, debug);
    }

    private void runGameLoop(int startPlayer, boolean debug){
        int rounds = 0;
        while(rounds < 1){
            rounds++;
            for(int i = 0; i < players.size(); i++){
                Player currentPlayer = players.get(i + startPlayer < players.size() ? i + startPlayer : i - (players.size() - startPlayer));
                System.out.printf(debug ? "Player %d's turn (i = %d)\n" : "Player %d's turn...\n",currentPlayer.getNum(), i);
                runTurn(currentPlayer);
            }
        }
    }

    private void runTurn(Player player){
        rollAllDice();
        int rolls = 1;
        String in;
        do {
            System.out.printf("\t%d rolls left. Would you like to roll again (y/n)?\n", 3-rolls);
            in = sc.nextLine();
            while(!(in.equalsIgnoreCase("y") || in.equalsIgnoreCase("n"))){
                System.out.println("\tWhat was that? (y/n)");
                in = sc.nextLine();
            }
            if(in.equalsIgnoreCase("n"))
                break;
            System.out.println("\tWhich dice would you like to reroll? Enter numbers in comma separated line, i.e. 2,3,5");
            in = sc.nextLine();
            for(char c : in.toCharArray()){
                int toReroll = Character.getNumericValue(c) - 1;
                if(toReroll > 0 && toReroll < 5)
                    dice[toReroll].getValue(true);
            }
            rolls++;
            showDice();
        }while(rolls < 3);
        System.out.println("\tHere come the scores...");
        calcPossibleScores();
    }

    private void rollAllDice(){
        for(int i = 0; i < dice.length; i++)
            System.out.printf("\tDice %d showing %d\n", i+1, dice[i].getValue(true));
    }

    private void showDice(){
        for(int i = 0; i < dice.length; i++)
            System.out.printf("\tDice %d showing %d\n", i+1, dice[i].getValue(false));
    }

    private void calcPossibleScores(){
        System.out.printf("\tUpper Ones Score: %d\n",calcSingleTotal(1));
        System.out.printf("\tUpper Twos Score: %d\n",calcSingleTotal(2));
        System.out.printf("\tUpper Threes Score: %d\n",calcSingleTotal(3));
        System.out.printf("\tUpper Fours Score: %d\n",calcSingleTotal(4));
        System.out.printf("\tUpper Fives Score: %d\n",calcSingleTotal(5));
        System.out.printf("\tUpper Sixes Score: %d\n",calcSingleTotal(6));

        System.out.printf("\tLower 3 of a Kind Score: %d\n",calcLowerScore(LowerCats.ThreeOfAKind));
        System.out.printf("\tLower 4 of a Kind Score: %d\n",calcLowerScore(LowerCats.FourOfAKind));
        System.out.printf("\tLower Full House Score: %d\n",calcLowerScore(LowerCats.FullHouse));
    }

    private int calcSingleTotal(int num){
        int total = 0;
        for(Dice die: dice){
            if (die.getValue(false) == num)
                total += num;
        }
        return total;
    }

    private int calcLowerScore(LowerCats lowerCats){
        int total = 0;
        if(lowerCats == LowerCats.ThreeOfAKind){
            int sames[] = {0,0,0,0,0};
            for(int i = 0; i < dice.length; i++) {
                for (int j = 0; j < dice.length; j++) {
                    if(j != i){
                        if(dice[i].getValue(false) == dice[j].getValue(false))
                            sames[i]++;
                    }
                }
            }
            for(int same : sames){
                if(same >= 2)
                    total = sumDice();
            }
        }
        if(lowerCats == LowerCats.FourOfAKind){
            int sames[] = {0,0,0,0,0};
            for(int i = 0; i < dice.length; i++) {
                for (int j = 0; j < dice.length; j++) {
                    if(j != i){
                        if(dice[i].getValue(false) == dice[j].getValue(false))
                            sames[i]++;
                    }
                }
            }
            for(int same : sames){
                if(same >= 2)
                    total = sumDice();
            }
        }
        if(lowerCats == LowerCats.FullHouse){
            int sames[] = {0,0,0,0,0};
            for(int i = 0; i < dice.length; i++) {
                for (int j = 0; j < dice.length; j++) {
                    if(j != i){
                        if(dice[i].getValue(false) == dice[j].getValue(false))
                            sames[i]++;
                    }
                }
            }
            boolean full = false, house = false;
            for(int same : sames){
                if(same == 2)
                    full = true;
                if(same == 1)
                    house = true;
            }
            if(full && house)
                total = 25;
        }
        return total;
    }

    private int sumDice(){
        int total = 0;
        for(Dice die : dice)
            total += die.getValue(false);
        return total;
    }
    private class Player{
        private final int num;
        private int[] upperScores;
        private int[] lowerScores;

        private Player(int playerNum){
            num = playerNum;
            this.upperScores = new int[]{-1,-1,-1,-1,-1,-1};
            this.lowerScores = new int[]{-1,-1,-1,-1,-1,-1,-1};
        }

        public int getNum(){
            return num;
        }

        public int getUpperScores(int index){
            return upperScores[index];
        }

        public int getLowerScores(int index){
            return lowerScores[index];
        }

        public void setUpperScores(int index, int value){
            this.upperScores[index] = value;
        }

        public void setLowerScores(int index, int value){
            this.lowerScores[index] = value;
        }

        public int getTotalScore(){
            int total = 0;
            int upperTotal = 0;
            for(int i : upperScores) {
                total += i;
                upperTotal += i;
            }
            for(int i : lowerScores)
                total += i;
            if(upperTotal >= 63)
                total += 35;
            return total;
        }
    }
}
