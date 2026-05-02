// Реализация игры "Крестики-нолики" (3x3)
// Минимаксный алгоритм

package com.mycompany.app;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.Random;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;

enum State { PLAYING, OWIN, XWIN, DRAW };


class Player {
  public char symbol;
  public int move;
  public boolean selected;
  public boolean win;
}

class Game {
    public State state;
    public Player player1, player2;
    public Player cplayer; // текущий игрок
    public int nmove;  // последний шаг сделанный действующим игроком 
    public char symbol;
    public static final int INF = 100;
    public int q;
    public char[] board;


    public Game() {
      player1=new Player();
      player2=new Player();
      player1.symbol='X';
      player2.symbol='O';
      state=State.PLAYING; 
      board=new char[9];   // текущая доска в игре  
      for(int i=0;i<9;i++)
        board[i]=' ';
    }

    // возвращаем состояние игры
    public State checkState(char[] board) 
    {
      //char symbol=game.symbol;//cplayer.symbol;
      State state=State.PLAYING;
      if ((board[0] == symbol && board[1] == symbol && board[2] == symbol) ||
          (board[3] == symbol && board[4] == symbol && board[5] == symbol) ||
          (board[6] == symbol && board[7] == symbol && board[8] == symbol) ||
          (board[0] == symbol && board[3] == symbol && board[6] == symbol) ||
          (board[1] == symbol && board[4] == symbol && board[7] == symbol) ||
          (board[2] == symbol && board[5] == symbol && board[8] == symbol) ||
          (board[0] == symbol && board[4] == symbol && board[8] == symbol) ||
          (board[2] == symbol && board[4] == symbol && board[6] == symbol)) 
      {
        if (symbol == 'X')   
            state = State.XWIN;
        else if (symbol == 'O')  
            state = State.OWIN;
      }
      else {
        state = State.DRAW;
        for (int i = 0; i < 9; i++) 
        {
            if (board[i] == ' ') {
                state = State.PLAYING;
                break;
            }
        }
    }
    return state;
  }
     // сгенерировать возможные ходы
   void generateMoves(char[] board, ArrayList<Integer> move_list) {
    for (int i = 0; i < 9; i++) 
        if (board[i] == ' ') 
            move_list.add(i);
   }

   // оценка позиции
   int evaluatePosition(char[] board, Player player)  
   {
    State state=checkState(board);
    if ((state == State.XWIN || state == State.OWIN || state == State.DRAW)) 
    {
        if ((state == State.XWIN && player.symbol == 'X') || (state == State.OWIN && player.symbol == 'O')) 
            return +Game.INF;
        else if ((state == State.XWIN && player.symbol == 'O') || (state == State.OWIN && player.symbol == 'X')) 
            return -Game.INF;
        else if (state == State.DRAW) 
            return 0;
    }
    return -1;
   }

   int MiniMax(char[] board, Player player) // выбор наилучшего хода
   {
    int best_val = -Game.INF, index = 0;
    ArrayList<Integer> move_list=new ArrayList<>();
    int[] best_moves = new int[9];
 
    generateMoves(board, move_list); 

    while (move_list.size()!=0) { 
        board[move_list.get(0)] = player.symbol; 
        symbol = player.symbol;
 
       
        int val = MinMove(board, player); 
       

        if (val > best_val) { 
            best_val = val;
            index = 0;
            best_moves[index] = move_list.get(0)+1; 
        }
        else if (val == best_val)
            best_moves[++index] = move_list.get(0)+1; 
 
        System.out.printf("\nminimax: %3d(%1d) ", 1 + move_list.get(0), val);
        board[move_list.get(0)] = ' '; 
        move_list.remove(0);
    }
    if (index > 0)  {
      Random r = new Random();
      index = r.nextInt(index);
    }
   
    System.out.printf("\nminimax best: %3d(%1d) ", best_moves[index], best_val);
    System.out.printf("Steps counted: %d", q);
    q = 0;
    return best_moves[index];
  }
  
  int MinMove(char[] board, Player player)  {

    int pos_value = evaluatePosition(board, player); 
    if (pos_value != -1) 
      return pos_value;
    q++;
    int best_val = +Game.INF;
    ArrayList<Integer> move_list=new ArrayList<>();
    
    generateMoves(board, move_list); 

    while (move_list.size()!=0) { 
        symbol= (player.symbol == 'X') ? 'O' : 'X'; 
        board[move_list.get(0)] = symbol; 

        int val = MaxMove(board, player); 
        
        if (val < best_val) {
            best_val = val;  
        }
        board[move_list.get(0)] = ' ';
        move_list.remove(0);
    }
    return best_val;
  }

  int MaxMove(char[] board, Player player) {
    int pos_value = evaluatePosition(board, player);
    if (pos_value != -1) 
      return pos_value;
    q++;
    int best_val = -Game.INF;
    ArrayList<Integer> move_list=new ArrayList<>();
    generateMoves(board, move_list);
    while (move_list.size()!=0) {
        symbol=(player.symbol == 'X') ? 'X' : 'O'; 
        board[move_list.get(0)] = symbol;
        int val = MinMove(board, player);
        if (val > best_val) {
            best_val = val;
        }
        board[move_list.get(0)] = ' ';
        move_list.remove(0);
    }
    return best_val;
  }
}

public class Program {

    public static FileWriter fileWriter;
    public static PrintWriter printWriter;
    public static void main(String[] args) throws IOException {
       JFrame frame = new JFrame("Demo");
       frame.add(new TicTacToePanel(new GridLayout(3,3)));
       frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       frame.setBounds(5, 5, 500, 500);
       frame.setVisible(true);
    }
}

class TicTacToeCell extends JButton {
    private boolean isFill;
    private int num;
    private int row;
    private int col;
    private char marker;

    public TicTacToeCell(int num,int x,int y) {
        this.num=num;
        row=y;
        col=x;
        marker=' ';
        setText(Character.toString(marker));
        setFont(new Font("Arial", Font.PLAIN, 40));
    }
    public void setMarker(String m) {
        marker=m.charAt(0);
        setText(m);
        setEnabled(false);
    }
    public char getMarker() {
        return marker;
    }
    public int getRow() {
        return row;
    }
    public int getCol() {
        return col;
    }
    public int getNum() {
        return num;
    }

}

class Utility {

  public static void print(char[] board) {
    System.out.println();
        for(int j=0;j<9;j++)
          System.out.print(board[j]+"-");
        System.out.println();
  }
  public static void print(int[] board) {
    System.out.println();
        for(int j=0;j<9;j++)
          System.out.print(board[j]+"-");
        System.out.println();
  }  
  public static void print(ArrayList<Integer> moves) {
    System.out.println();
        for(int j=0;j<moves.size();j++)
          System.out.print(moves.get(j)+"-");
        System.out.println();
  }  
}

class TicTacToePanel extends JPanel implements ActionListener {

   private Game game;

   private void createCell(int num,int x,int y) {
       cells[num]=new TicTacToeCell(num,x,y);
       cells[num].addActionListener(this);
       add(cells[num]);

   }

   private TicTacToeCell[] cells = new TicTacToeCell[9];
   TicTacToePanel(GridLayout layout) {
       super(layout);
       createCell(0,0,0);
       createCell(1,1,0);
       createCell(2,2,0);
       createCell(3,0,1);
       createCell(4,1,1);
       createCell(5,2,1);
       createCell(6,0,2);
       createCell(7,1,2); 
       createCell(8,2,2);
       game=new Game();
       game.cplayer=game.player1;
   }

   public void actionPerformed(ActionEvent ae) {
      game.player1.move = -1;
      game.player2.move = -1;
      //System.out.println(game.cplayer.symbol);
      //System.out.println(((TicTacToeCell)(ae.getSource())).getNum());


      int i=0;
      for(TicTacToeCell jb: cells) {
         if(ae.getSource()==jb) {
            jb.setMarker(Character.toString(game.cplayer.symbol));
         }
         game.board[i++]=jb.getMarker();
      }
      if(game.cplayer==game.player1) {

         game.player2.move = game.MiniMax(game.board, game.player2);
         game.nmove = game.player2.move;
         game.symbol = game.player2.symbol;
         game.cplayer = game.player2;
         if(game.player2.move>0)
            cells[game.player2.move-1].doClick();
       }
       else
       {
         game.nmove = game.player1.move;
         game.symbol = game.player1.symbol;
         game.cplayer = game.player1;
       }

      game.state=game.checkState(game.board);


      if(game.state==State.XWIN) {
        JOptionPane.showMessageDialog(null,"Выиграли крестики","Результат", JOptionPane.WARNING_MESSAGE);
        System.exit(0);

      }
      else if(game.state==State.OWIN) {
        JOptionPane.showMessageDialog(null,"Выиграли нолики","Результат", JOptionPane.WARNING_MESSAGE);
        System.exit(0);
      }
      else if(game.state==State.DRAW) {
        JOptionPane.showMessageDialog(null,"Ничья","Результат", JOptionPane.WARNING_MESSAGE);
        System.exit(0);
      } 




   }
}


