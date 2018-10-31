import java.util.ArrayList;
import java.util.Arrays;
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

    private Scanner sc;
    private ArrayList<Player> players;
    private Dice[] dice;
    private int rounds;
    private String ifykyk = "000000";

    public static void main(String[] args) {
        Scanner tempScan = new Scanner(System.in);
        System.out.printf("Welcome to Yahtzee! How many players? (Max %d)\n", MAX_PLAYERS);
        String input = tempScan.nextLine();
        int players;
        try{
            players = Integer.parseInt(input);
        }catch (NumberFormatException e){
            players = -1;
        }
        while(!(players > 0 && players < MAX_PLAYERS + 1)){
            System.out.println("Whoops! Invalid number of players. Try again.");
            input = tempScan.nextLine();
            try{
                players = Integer.parseInt(input);
            }catch (NumberFormatException e){
                players = -1;
            }
        }
        System.out.printf("Starting game with %d players\n", players);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println();
        UrishDiceGame mainGame = new UrishDiceGame(players);
        mainGame.start();
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

    public void start(){
        System.out.println("Press Enter to Start!");
        set(sc.nextLine());
        int firstPlayerIndex = 0;
        if(!eval(2)){
            System.out.println("Everyone rolls to see who goes first");
            try {
                if(!eval(0))
                    Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int[] rolls = new int[players.size()];
            for(Player player : players){
                int total = 0;
                System.out.printf("Player %d rolling...\n", player.getNum());
                try {
                    if(!eval(0))
                        Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for(int i = 0; i < dice.length; i++) {
                    System.out.printf("\tRoll %d, rolled a %d\n", i+1, dice[i].getValue(true));
                    try {
                        if(!eval(0))
                            Thread.sleep(250);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    total += dice[i].getValue(false);
                }
                System.out.printf("\tTotal roll: %d\n", total);
                rolls[player.num - 1] = total;
                try {
                    if(!eval(0))
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
        System.out.printf(eval(1) ? "Player %d goes first. (index = %d)\n" : "Player %d goes first.\n", firstPlayerIndex + 1, firstPlayerIndex);
        runGameLoop(firstPlayerIndex);
    }

    private void runGameLoop(int startPlayer){
        rounds = 0;
        while(rounds < 13){
            rounds++;
            System.out.printf("Round %d\n", rounds);
            try {
                if(!eval(0))
                    Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for(int i = 0; i < players.size(); i++){
                Player currentPlayer = players.get(i + startPlayer < players.size() ? i + startPlayer : i - (players.size() - startPlayer));
                System.out.printf(eval(1) ? "Player %d's turn (i = %d)\n" : "Player %d's turn...\n",currentPlayer.getNum(), i);
                runTurn(currentPlayer);
            }
        }
    }

    private void runTurn(Player player){
        if(eval(3)){
            if(rounds <= 6)
                player.setUpperScores(rounds - 1, 50);
            else
                player.setLowerScores(rounds - 7, 50);
            printScorecard(player,true);
            return;
        }
        System.out.printf("Player %d, are you ready? Press Enter\n", player.getNum());
        sc.nextLine();
        printScorecard(player, true);
        System.out.println("\tPress Enter to roll...");
        sc.nextLine();
        rollAllDice();
        int rolls = 1;
        String in;
        do {
            System.out.printf("\t%d rolls left. Would you like to roll again (y/n)?\n", 3-rolls);
            System.out.print("\t");
            in = sc.nextLine();
            if(in.equalsIgnoreCase("pick") && eval(5)){
                in = sc.nextLine();
                int die = 0;
                for(char c : in.toCharArray()){
                    if(Character.getNumericValue(c) > 0){
                        this.dice[die].setValue(Character.getNumericValue(c));
                        die++;
                    }
                }
                showDice(false);
                break;
            }
            set(in);
            if(eval(4)){
                for(Dice die : dice)
                    die.setValue(6);
                showDice(false);
                break;
            }
            while(!(in.equalsIgnoreCase("y") || in.equalsIgnoreCase("n"))){
                System.out.println("\tWhat was that? (y/n)");
                System.out.print("\t");
                in = sc.nextLine();
            }
            if(in.equalsIgnoreCase("n"))
                break;
            System.out.println("\tWhich dice would you like to reroll? Enter numbers in comma separated line, i.e. 2,3,5");
            System.out.print("\t");
            in = sc.nextLine();
            for(char c : in.toCharArray()){
                int toReroll = Character.getNumericValue(c) - 1;
                if(toReroll >= 0 && toReroll < 5) {
                    int old = dice[toReroll].getValue(false);
                    while(dice[toReroll].getValue(false) == old)
                        dice[toReroll].getValue(true);
                }
            }
            rolls++;
            showDice(false);
        }while(rolls < 3);
        boolean jokerRules = false;
        if(calcLowerScore(LowerCats.YAHTZEE, jokerRules) > 1 && player.getLowerScores(LowerCats.YAHTZEE.ordinal()) == 0){
            System.out.println("\tJOKER RULES! PICK A BOX TO FILL! (1-13)");
            jokerRules = true;
        }else{
            System.out.println("\tWhich box would you like to fill? Enter a number 1-13");
        }
        try {
            if(!eval(0))
                Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        calcPossibleScores(jokerRules, player);
        boolean filled = false;
        while(!filled) {
            System.out.print("\t");
            String input = sc.nextLine();
            int boxNum;
            try {
                boxNum = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                boxNum = -1;
            }
            while (!(boxNum > 0 && boxNum < 14)) {
                System.out.println("\tWhoops! Invalid box number. Try again.");
                System.out.print("\t");
                input = sc.nextLine();
                try {
                    boxNum = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    boxNum = -1;
                }
            }
            if (boxNum <= 6){
                if(player.getUpperScores(boxNum - 1) != -1){
                    System.out.println("\tThat box is already filled! Try again");
                }else{
                    player.setUpperScores(boxNum - 1, calcSingleTotal(boxNum));
                    filled = true;
                }
            }else{
                boxNum -= 6;
                if(player.getLowerScores(boxNum - 1) == -1){
                    player.setLowerScores(boxNum - 1, calcLowerScore(LowerCats.values()[boxNum-1],jokerRules));
                    filled = true;
                }else if(!jokerRules && (boxNum - 1) == LowerCats.YAHTZEE.ordinal() && player.getLowerScores(LowerCats.YAHTZEE.ordinal()) >= 50){
                    player.setLowerScores(LowerCats.YAHTZEE.ordinal(), player.getLowerScores(LowerCats.YAHTZEE.ordinal()) + 100);
                    System.out.println("\tJOKER RULES! PICK ANOTHER BOX TO FILL! (1-13)");
                    jokerRules = true;
                }else{
                    System.out.println("\tThat box is already filled! Try again");
                }
            }
        }
        printScorecard(player, true);
    }

    private void rollAllDice(){
        showDice(true);
    }

    private void showDice(boolean reroll){
        for(int i = 0; i < dice.length; i++) {
            System.out.printf("\tDice %d showing %d\n", i + 1, dice[i].getValue(reroll));
            try {
                if(!eval(0))
                    Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(calcLowerScore(LowerCats.YAHTZEE, false) > 1)
            System.out.println("\t!!! YAHTZEE !!!");
    }

    private void printScorecard(Player player, boolean tabbed){
        String tab = tabbed ? "\t" : "";
        System.out.printf("%sPlayer %d's Scorecard:\n", tab, player.getNum());
        try {
            if(!eval(0))
                Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.printf("%sUpper Ones Score: %s\n", tab, player.getUpperScoresString(0));
        System.out.printf("%sUpper Twos Score: %s\n", tab, player.getUpperScoresString(1));
        System.out.printf("%sUpper Threes Score: %s\n", tab, player.getUpperScoresString(2));
        System.out.printf("%sUpper Fours Score: %s\n", tab, player.getUpperScoresString(3));
        System.out.printf("%sUpper Fives Score: %s\n", tab, player.getUpperScoresString(4));
        System.out.printf("%sUpper Sixes Score: %s\n", tab, player.getUpperScoresString(5));
        System.out.printf("%sTotal Upper Score: %d%s\n", tab, player.getTotalScore(0, true),player.getTotalScore(0, true)>=63?" + 35":"");

        System.out.printf("%sLower 3 of a Kind Score: %s\n", tab, player.getLowerScoresString(0));
        System.out.printf("%sLower 4 of a Kind Score: %s\n", tab, player.getLowerScoresString(1));
        System.out.printf("%sLower Full House Score: %s\n", tab, player.getLowerScoresString(2));
        System.out.printf("%sLower Small Straight Score: %s\n", tab, player.getLowerScoresString(3));
        System.out.printf("%sLower Large Straight Score: %s\n", tab, player.getLowerScoresString(4));
        System.out.printf("%sLower YAHTZEE Score: %s\n", tab, player.getLowerScoresString(5));
        System.out.printf("%sLower Chance Score: %s\n", tab, player.getLowerScoresString(6));
        System.out.printf("%sTotal Lower Score: %d\n", tab, player.getTotalScore(1, true));

        System.out.printf("%sTotal Score: %d\n", tab, player.getTotalScore(-1, true));
        try {
            if(!eval(0))
                Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void calcPossibleScores(boolean jokerRules, Player player){
        if(player.getUpperScores(0) == -1) System.out.printf("\t(1) Upper Ones Score: %d \n",calcSingleTotal(1));
        if(player.getUpperScores(1) == -1) System.out.printf("\t(2) Upper Twos Score: %d \n",calcSingleTotal(2));
        if(player.getUpperScores(2) == -1) System.out.printf("\t(3) Upper Threes Score: %d \n",calcSingleTotal(3));
        if(player.getUpperScores(3) == -1) System.out.printf("\t(4) Upper Fours Score: %d \n",calcSingleTotal(4));
        if(player.getUpperScores(4) == -1) System.out.printf("\t(5) Upper Fives Score: %d \n",calcSingleTotal(5));
        if(player.getUpperScores(5) == -1) System.out.printf("\t(6) Upper Sixes Score: %d \n",calcSingleTotal(6));
        if(player.getLowerScores(0) == -1) System.out.printf("\t(7) Lower 3 of a Kind Score: %d \n",calcLowerScore(LowerCats.ThreeOfAKind, jokerRules));
        if(player.getLowerScores(1) == -1) System.out.printf("\t(8) Lower 4 of a Kind Score: %d \n",calcLowerScore(LowerCats.FourOfAKind, jokerRules));
        if(player.getLowerScores(2) == -1) System.out.printf("\t(9) Lower Full House Score: %d \n",calcLowerScore(LowerCats.FullHouse, jokerRules));
        if(player.getLowerScores(3) == -1) System.out.printf("\t(10) Lower Small Straight Score: %d \n",calcLowerScore(LowerCats.SmallStraight, jokerRules));
        if(player.getLowerScores(4) == -1) System.out.printf("\t(11) Lower Large Straight Score: %d \n",calcLowerScore(LowerCats.LargeStraight, jokerRules));
        if(player.getLowerScores(5) == -1) System.out.printf("\t(12) Lower YAHTZEE Score: %d \n",calcLowerScore(LowerCats.YAHTZEE, jokerRules));
        if(player.getLowerScores(6) == -1) System.out.printf("\t(13) Lower Chance Score: %d \n",calcLowerScore(LowerCats.Chance, jokerRules));
    }

    private int calcSingleTotal(int num){
        int total = 0;
        for(Dice die: dice){
            if (die.getValue(false) == num)
                total += num;
        }
        return total;
    }

    private int calcLowerScore(LowerCats category, boolean jokerRules){
        int total = 0;
        if(category == LowerCats.ThreeOfAKind){
            int sames[] = new int[dice.length];
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
            if(jokerRules)
                total = sumDice();
        }
        if(category == LowerCats.FourOfAKind){
            int sames[] = new int[dice.length];
            for(int i = 0; i < dice.length; i++) {
                for (int j = 0; j < dice.length; j++) {
                    if(j != i){
                        if(dice[i].getValue(false) == dice[j].getValue(false))
                            sames[i]++;
                    }
                }
            }
            for(int same : sames){
                if(same >= 3)
                    total = sumDice();
            }
            if(jokerRules)
                total = sumDice();
        }
        if(category == LowerCats.FullHouse){
            int sames[] = new int[dice.length];
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
            if((full && house) || jokerRules)
                total = 25;
        }
        if(category == LowerCats.SmallStraight){
            int values[] = new int[dice.length];
            for(int i = 0; i < dice.length; i++)
                values[i] = dice[i].getValue(false);
            Arrays.sort(values);
            int straightLength = 1;
            int lastValue = values[0];
            for(int i = 1; i < values.length; i++){
                if(values[i] == lastValue + 1)
                    straightLength++;
                else
                    straightLength = 1;
                lastValue = values[i];
            }
            if(straightLength >= 4 || jokerRules)
                total = 30;
        }
        if(category == LowerCats.LargeStraight){
            int values[] = new int[dice.length];
            for(int i = 0; i < dice.length; i++)
                values[i] = dice[i].getValue(false);
            Arrays.sort(values);
            int straightLength = 1;
            int lastValue = values[0];
            for(int i = 1; i < values.length; i++){
                if(values[i] == lastValue + 1)
                    straightLength++;
                else
                    straightLength = 1;
                lastValue = values[i];
            }
            if(straightLength >= 5 || jokerRules)
                total = 40;
        }
        if(category == LowerCats.YAHTZEE){
            int sames[] = new int[dice.length];
            for(int i = 0; i < dice.length; i++) {
                for (int j = 0; j < dice.length; j++) {
                    if(j != i){
                        if(dice[i].getValue(false) == dice[j].getValue(false))
                            sames[i]++;
                    }
                }
            }
            for(int same : sames){
                if(same >= dice.length - 1){
                    total = 50;
                }
            }
        }
        if(category == LowerCats.Chance)
            total = sumDice();
        return total;
    }

    private int sumDice(){
        int total = 0;
        for(Dice die : dice)
            total += die.getValue(false);
        return total;
    }

    private boolean eval(int i){
        return ifykyk.charAt(i) == '1';
    }

    //fast 0
    //debug 1
    //noroll 2
    //perf 3
    //allyahtzee 4
    //nolegit 5
    private void set(String input){
        boolean changed = false;
        char[] chars = ifykyk.toCharArray();
        for(char c : input.toCharArray()){
            int i = Character.getNumericValue(c);
            if(i >= 0 && i < 6) {
                chars[i] = '1';
                chars[5] = '1';
                changed = true;
            }
        }
        if(changed) {
            ifykyk = new String(chars);
            System.out.println(";) " + ifykyk);
        }
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

        public String getUpperScoresString(int index){
            return getUpperScores(index) == -1 ? "Blank" : String.valueOf(getUpperScores(index));
        }

        public int getLowerScores(int index){
            return lowerScores[index];
        }

        public String getLowerScoresString(int index){
            return getLowerScores(index) == -1 ? "Blank" : String.valueOf(getLowerScores(index));
        }

        public void setUpperScores(int index, int value){
            this.upperScores[index] = value;
        }

        public void setLowerScores(int index, int value){
            this.lowerScores[index] = value;
        }

        //section = 0 for upper score
        //section = 1 for lower score
        //section = anything else for total
        public int getTotalScore(int section, boolean nice){
            int total = 0;
            int upperTotal = 0;
            for(int i : upperScores) {
                total += i >= 0 ? i : 0;
                upperTotal += i >= 0 ? i : 0;
            }
            int lowerTotal = 0;
            for(int i : lowerScores) {
                total += i >= 0 ? i : 0;
                lowerTotal += i >= 0 ? i : 0;
            }
            if(upperTotal >= 63)
                total += 35;
            if(section == 0){
                return nice ? Math.max(upperTotal,0) : upperTotal;
            }else if(section == 1){
                return nice ? Math.max(lowerTotal,0) : lowerTotal;
            }else{
                return nice ? Math.max(total,0) : total;
            }
        }
    }
}
