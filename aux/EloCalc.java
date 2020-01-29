import java.util.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.io.File;
import java.io.FileNotFoundException;
import static java.util.stream.Collectors.*;

// Test formula at https://www.desmos.com/calculator/ax6sezgrif
public class EloCalc {
    public static void main(String[] args) {
        Scanner in;

        try {
            in = new Scanner(new File("games.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            in = new Scanner(System.in);
        }

        HashMap<String, Integer> ratings = new HashMap<>();

        ArrayList<String> games = new ArrayList<>();

        int defaultElo = 1000;

        while (in.hasNext()) {
            String line = in.nextLine();
            String[] split = line.split(" ");

            String player1 = split[0].replaceAll("_", " ");
            String player2 = split[2].replaceAll("_", " ");
            int player1Score = Integer.parseInt(split[1]);
            int player2Score = Integer.parseInt(split[3]);
            boolean p1New = false;
            boolean p2New = false;

            // Give default elo if new player
            if (!ratings.containsKey(player1)) {
                ratings.put(player1, defaultElo);
                p1New = true;
            }

            if (!ratings.containsKey(player2)) {
                ratings.put(player2, defaultElo);
                p2New = true;
            }

            // Make higher rating player 1
            // If equal first alphabetically is player 1
            boolean player2GreaterRating = ratings.get(player2) > ratings.get(player1);
            boolean lexographicOrder = player1.compareTo(player2) > 1;
            boolean equalRatings = (int) ratings.get(player1) == (int) ratings.get(player2);

            if (player2GreaterRating || (lexographicOrder && equalRatings)) {
                String tempPlayer = player1;
                player1 = player2;
                player2 = tempPlayer;

                int tempScore = player1Score;
                player1Score = player2Score;
                player2Score = tempScore;

            }

            // Calculate transformed rating of each player
            double player1Transformed = Math.pow(10, ratings.get(player1) / 400.0);
            double player2Transformed = Math.pow(10, ratings.get(player2) / 400.0);
            double addedTransform = player1Transformed + player2Transformed;

            // Calculate expected scores
            double expected1 = player1Transformed / addedTransform;
            double expected2 = player2Transformed / addedTransform;

            // Find scores
            // One score will evaluate to 1, the other to 0
            double score1 = ((player1Score - player2Score) >> 31) + 1;
            double score2 = 1 - score1;

            // Calculate new ratings
            int k1 = getK(p1New, p2New, ratings.get(player1));
            int k2 = getK(p2New, p1New, ratings.get(player2));
            int newRating1 = (int) Math.round((ratings.get(player1) + k1 * (score1 - expected1)));
            int newRating2 = (int) Math.round((ratings.get(player2) + k2 * (score2 - expected2)));

            if (newRating1 == ratings.get(player1)) {
                if (score1 == 1) {
                    newRating1++;
                    newRating2--;
                } else {
                    newRating1--;
                    newRating2++;
                }
            }

            int player1Change = newRating1 - ratings.get(player1);
            int player2Change = newRating2 - ratings.get(player2);
            String change1String = Integer.toString(player1Change);
            String change2String = Integer.toString(player2Change);

            String styled1 = score1 > 0 ? "**" + player1 + "**" : player1;
            String styled2 = score2 > 0 ? "**" + player2 + "**" : player2;
            String styledScore1 = score1 > 0 ? "**" + player1Score + "**" : player1Score + "";
            String styledScore2 = score2 > 0 ? "**" + player2Score + "**" : player2Score + "";

            games.add((score1 > 0 ? "+" : "") + change1String + " | " + ratings.get(player1) + " | " + styled1 + " | "
                    + styledScore1 + " | " + styledScore2 + " | " + styled2 + " | " + ratings.get(player2) + " | "
                    + (score2 > 0 ? "+" : "") + change2String + "\n");

            ratings.put(player1, newRating1);
            ratings.put(player2, newRating2);
        }

        in.close();

        // Sort players by rating
        Map<String, Integer> rankings = ratings.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));

        String rankString = "Player | Score\n--- | ---\n";
        for (String name : rankings.keySet())
            rankString = rankString + name + " | " + rankings.get(name) + "\n";

        // Add header first then reverse list
        // Puts games in reverse chronological order, with most recent first
        games.add(
                "Change | Rating | Player | Score | Score | Player | Rating | Change\n--- | --- | --- | --- | --- | ---\n");
        Collections.reverse(games);

        String gameString = "";

        for (String s : games)
            gameString += s;

        System.out.println("## Rankings\n");
        System.out.println(rankString);
        System.out.println("## Games\n");
        System.out.println(gameString);
    }

    public static int getK(boolean isNew, boolean otherNew, int rating) {
        if (otherNew)
            return 10;

        if (isNew)
            return 40;

        if (rating < 2400)
            return 20;

        return 10;
    }
}