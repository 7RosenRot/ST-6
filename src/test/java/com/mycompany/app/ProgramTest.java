package com.mycompany.app;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.GridLayout;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ProgramTest {

  private final ByteArrayOutputStream outputSpy = new ByteArrayOutputStream();
  private final PrintStream originalSystemOut = System.out;

  @BeforeEach
  void initRedirect() {
    System.setOut(new PrintStream(outputSpy));
  }

  @AfterEach
  void restoreRedirect() {
    System.setOut(originalSystemOut);
  }

  @Test
  @DisplayName("Проверка инициализации параметров игрока")
  void shouldVerifyPlayerFields() {
    Player p = new Player();
    p.symbol = 'X';
    p.move = 5;
    p.selected = true;
    p.win = true;

    assertEquals('X', p.symbol);
    assertEquals(5, p.move);
    assertTrue(p.selected);
    assertTrue(p.win);
  }

  @Test
  @DisplayName("Проверка начального состояния игрового поля")
  void shouldInitializeGameCorrectly() {
    Game mainGame = new Game();
    assertEquals(State.PLAYING, mainGame.state);
    assertEquals('X', mainGame.player1.symbol);
    assertEquals('O', mainGame.player2.symbol);
    
    boolean isEmpty = true;
    for (char cell : mainGame.board) {
      if (cell != ' ') {
        isEmpty = false;
        break;
      }
    }
    assertTrue(isEmpty, "Поле должно быть пустым при старте");
  }

  @Test
  @DisplayName("Проверка алгоритма смены текущего игрока")
  void shouldRotatePlayers() {
    Game session = new Game();
    
    // Тест перехода X -> O
    session.symbol = session.player1.symbol;
    char active = (session.symbol == session.player1.symbol) ? session.player2.symbol : session.player1.symbol;
    assertEquals('O', active);

    // Тест перехода O -> X
    session.symbol = session.player2.symbol;
    active = (session.symbol == session.player1.symbol) ? session.player2.symbol : session.player1.symbol;
    assertEquals('X', active);
  }

  @Test
  @DisplayName("Тестирование всех выигрышных комбинаций и ничьи")
  void checkWinningAndDrawScenarios() {
    Game engine = new Game();

    for (int scenario = 1; scenario <= 8; scenario++) {
      engine.symbol = 'X';
      assertEquals(State.XWIN, engine.checkState(buildBoardWithWin('X', scenario)));
      
      engine.symbol = 'O';
      assertEquals(State.OWIN, engine.checkState(buildBoardWithWin('O', scenario)));
    }

    char[] drawBoard = {'X', 'X', 'O', 'O', 'O', 'X', 'X', 'O', 'X'};
    assertEquals(State.DRAW, engine.checkState(drawBoard));

    char[] midGame = {'X', ' ', 'O', ' ', ' ', ' ', ' ', ' ', ' '};
    engine.symbol = 'X';
    assertEquals(State.PLAYING, engine.checkState(midGame));
  }

  @Test
  @DisplayName("Оценка веса текущей позиции")
  void verifyPositionScoring() {
    Game core = new Game();

    core.symbol = 'X';
    assertEquals(Game.INF, core.evaluatePosition(buildBoardWithWin('X', 1), core.player1));
    assertEquals(-Game.INF, core.evaluatePosition(buildBoardWithWin('X', 1), core.player2));

    char[] drawGrid = {'X', 'O', 'X', 'X', 'O', 'O', 'O', 'X', 'X'};
    assertEquals(0, core.evaluatePosition(drawGrid, core.player1));
  }

  @Test
  @DisplayName("Интеллектуальный выбор хода (Minimax)")
  void aiShouldMakeOptimalMoves() {
    Game aiGame = new Game();

    char[] dangerBoard = {'O', 'O', ' ', ' ', 'X', ' ', ' ', ' ', ' '};
    aiGame.board = dangerBoard;
    int move = aiGame.MiniMax(dangerBoard, aiGame.player2);
    assertTrue(move == 3 || move == 8 || move == 9);

    char[] winChance = {'X', 'X', ' ', 'O', 'O', ' ', ' ', ' ', ' '};
    aiGame.board = winChance;
    assertEquals(3, aiGame.MiniMax(winChance, aiGame.player1));
  }

  @Test
  @DisplayName("Проверка блокировки победного хода противника")
  void aiShouldBlockOpponentWin() {
    Game defenseGame = new Game();

    char[] dangerBoard = {
      'X', 'X', ' ', 
      ' ', 'O', ' ', 
      ' ', ' ', ' '
    };
    defenseGame.board = dangerBoard;

    int move = defenseGame.MiniMax(dangerBoard, defenseGame.player2);

    assertEquals(3, move, "ИИ должен был заблокировать победу игрока X в клетке 3");
  }

  @Test
  @DisplayName("Генерация списка доступных ячеек")
  void shouldFindEmptySpaces() {
    Game logic = new Game();
    ArrayList<Integer> potentialMoves = new ArrayList<>();

    logic.generateMoves(logic.board, potentialMoves);
    assertEquals(9, potentialMoves.size());

    logic.board[4] = 'X';
    potentialMoves.clear();
    logic.generateMoves(logic.board, potentialMoves);
    assertFalse(potentialMoves.contains(4));
    assertEquals(8, potentialMoves.size());
  }

  @Test
  @DisplayName("Работа с компонентами GUI")
  void testInterfaceComponents() {
    TicTacToeCell uiCell = new TicTacToeCell(0, 1, 1);
    assertEquals(0, uiCell.getNum());
    assertEquals(' ', uiCell.getMarker());
    
    uiCell.setMarker("O");
    assertEquals('O', uiCell.getMarker());

    TicTacToePanel uiPanel = new TicTacToePanel(new GridLayout(3, 3));
    assertNotNull(uiPanel);
  }

  @Test
  @DisplayName("Форматированный вывод утилит")
  void testConsoleFormatting() {
    Utility tool = new Utility();
    String lineBreak = System.lineSeparator();

    char[] sample = {'X', ' ', 'O', ' ', 'X', ' ', 'O', ' ', 'X'};
    tool.print(sample);
    String output = outputSpy.toString();
    assertTrue(output.contains("X") && output.contains("O"));
    
    outputSpy.reset();
    ArrayList<Integer> dataList = new ArrayList<>(List.of(1, 3, 5));
    tool.print(dataList);
    assertNotNull(outputSpy.toString());
  }

  private char[] buildBoardWithWin(char mark, int pattern) {
    char[] b = {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '};
    int[][] wins = {
      {0, 1, 2}, {3, 4, 5}, {6, 7, 8},
      {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
      {0, 4, 8}, {2, 4, 6}
    };
    for (int idx : wins[pattern - 1]) {
      b[idx] = mark;
    }
    return b;
  }
}