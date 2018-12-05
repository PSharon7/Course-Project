f_in = open("input.txt", "r");

rowLetter = ["A", "B", "C", "D", "E", "F", "G", "H"]
colNumber = ["1", "2", "3", "4", "5", "6", "7", "8"]

# global variants
stateDict = dict()
boardDict = dict()
utility = dict()
rowValue = []
nodeGenerated = 0

# class Board
# for board related operation
class Board:
    def __init__(self, board):
        self.boardState = board
        self.curPos = [[],[]]
        self.curPos[0] = self.calcPos("S")
        self.curPos[1] = self.calcPos("C")
        
    def calcPos(self, player):
        pos = []
        for i in range(8):
            for j in range(8): 
                if (self.boardState[i][j][0] == player):
                    pos.append((i,j))
  
        return pos

    def calcLegalMoves(self, player):
        legalMoves = []
        
        if (player == "S"):
            curPlayer = 0
            next = -1
            boardLimit = 0
            
        else:
            curPlayer = 1
            next = 1
            boardLimit = 7

        for pos in self.curPos[curPlayer]:
            if (pos[0] == boardLimit):
                continue
            # diagonal right
            if (pos[1] != 7):
                # empty
                if (self.boardState[pos[0] + next][pos[1] + 1] == "0"):
                    temp = (pos[0],pos[1]), (pos[0]+next, pos[1]+1)
                    legalMoves.append(temp)
        
                # same
                elif (self.boardState[pos[0] + next][pos[1] + 1][0] == player and (pos[0] + next == boardLimit)):
                    temp = (pos[0],pos[1]), (pos[0]+next, pos[1]+1)
                    legalMoves.append(temp)

                # has enemy
                elif (self.boardState[pos[0] + next][pos[1] + 1][0] != player and self.boardState[pos[0] + next][pos[1] + 1][0] != "0"):
                    jumps = self.checkJump((pos[0],pos[1]), False, player)
                    if (len(jumps) != 0):
                        legalMoves.extend(jumps)
                        
            if (pos[1] != 0):
                # empty
                if (self.boardState[pos[0] + next][pos[1] - 1] == "0"):
                    temp = (pos[0],pos[1]),(pos[0]+next, pos[1]-1)
                    legalMoves.append(temp)
        
                # same
                elif (self.boardState[pos[0] + next][pos[1] - 1][0] == player and (pos[0] + next == boardLimit)):
                    temp = (pos[0],pos[1]),(pos[0]+next, pos[1]-1)
                    legalMoves.append(temp)
        
                # has enemy
                elif (self.boardState[pos[0] + next][pos[1] - 1][0] != player and self.boardState[pos[0] + next][pos[1] - 1][0] != "0"):
                    jumps = self.checkJump((pos[0],pos[1]), True, player)
                    if (len(jumps) != 0):
                        legalMoves.extend(jumps)
                
        return legalMoves

    def checkJump(self, pos, isLeft, player):
        jumps = []
        if (player == "S"):
            curPlayer = 0
            next = -1
            boardLimit = 0
        else:
            curPlayer = 1
            next = 1
            boardLimit = 7

        if (pos[0] + next == boardLimit):
            return jumps

        if (isLeft):
            if (pos[1] > 1 and (self.boardState[pos[0]+next+next][pos[1]-2] == "0" or (self.boardState[pos[0]+next+next][pos[1]-2][0] == player and pos[0]+next+next == boardLimit))):
                temp = pos, (pos[0]+next+next, pos[1]-2)
                jumps.append(temp)
        else:
            if (pos[1] < 6 and (self.boardState[pos[0]+next+next][pos[1]+2] == "0" or (self.boardState[pos[0]+next+next][pos[1]+2][0] == player and pos[0]+next+next == boardLimit))):
                temp = pos, (pos[0]+next+next, pos[1]+2)
                jumps.append(temp)
        return jumps

    def modifyBoard(self, player, action):
        if (player == "S"):
            pos = 0
        else:
            pos = 1

        start = action[0]
        end = action[1]

        result = [row[:] for row in self.boardState]

        result[start[0]][start[1]] = "0"

        # capture
        if (abs(end[0] - start[0]) == 2):
            x = (end[0] + start[0])/2
            y = (end[1] + start[1])/2
            result[x][y] = "0"
        
            if (result[end[0]][end[1]] != '0'):
                tmp = str(int(result[end[0]][end[1]][1:]) + 1)
                result[end[0]][end[1]] = result[end[0]][end[1]][:1] + tmp
            else:
                result[end[0]][end[1]] = player + '1'

        else:
            if (result[end[0]][end[1]] != '0'):
                tmp = str(int(result[end[0]][end[1]][1:]) + 1)
                result[end[0]][end[1]] = result[end[0]][end[1]][:1] + tmp
            else:
                result[end[0]][end[1]] = player + '1'
        
        return result

    def terminalTest(self, nodePass):
        if (len(self.curPos[0]) == 0 or len(self.curPos[1]) == 0):
            return True;
        elif (len(self.calcLegalMoves("S")) == 0 and len(self.calcLegalMoves("C")) == 0 and nodePass == 2):
            return True
        else:
            return False

# class myGame
# for algorithm minimax & alpha-beta to get the game state
class myGame:
    def legalMoves(self, state):
        # sort the move so it satisfies the request
        return sorted(stateDict.get(state).keys())

    def utility(self, state, player):
        if (player == "max"):
            return utility[state]
        else:
            return -utility[state]

    def result(self, state, actions):
        return stateDict[state][actions];

    def terminal_Test(self, state):
        move = state.split("#")
        if (move[-1] == "pass" and move[-2] == "pass"):
            return True;

        curDepth = len(state.split("#")) - 1
        board = boardDict[state]
        b = Board(board)
        if (curDepth == maxDepth):
            return True
        elif (len(b.curPos[0]) == 0 or len(b.curPos[1]) == 0):
            return True
        else:
            return False            

    def player(self, state):
        curDepth = len(state.split("#")) - 1
        if (curDepth % 2 == 0):
            return "max"
        else:
            return "min"

# algorithm
# minimax
def minimaxDecision(state, game):
    player = game.player(state)

    def maxValue(state):
        if (game.terminal_Test(state)):
            value = game.utility(state, player)
            return value

        value = -float("inf")
        for action in game.legalMoves(state):
            value = max(value, minValue(game.result(state, action)))

        return value

    def minValue(state):
        if (game.terminal_Test(state)):
            value = game.utility(state, player)
            return value
        
        value = float("inf")
        for action in game.legalMoves(state):
            value = min(value, maxValue(game.result(state, action)))

        return value

    nextMove = None
    maxVal = -float("inf")

    try:
        for action in game.legalMoves(state):
            value = maxValue(game.result(state, action))    
            if value > maxVal:
                maxVal = value
                nextMove = action
        return nextMove, maxVal
    except:
        return None

# algorithm
# alpha-beta
def alphaBeta(state, game):
    player = game.player(state)

    def max_Value(state, alpha, beta):
        
        global nodeGenerated
        nodeGenerated += 1
        
        if (game.terminal_Test(state)):
            value = game.utility(state, player)
            return value

        value = -float("inf")
        for action in game.legalMoves(state):
            value = max(value, min_Value(game.result(state, action), alpha, beta))
        
            if (value >= beta):
                return value
            alpha = max(alpha, value)
        return value

    def min_Value(state, alpha, beta):
        
        global nodeGenerated
        nodeGenerated += 1
        
        if (game.terminal_Test(state)):
            value = game.utility(state, player)
            return value
        value = float("inf")
        for action in game.legalMoves(state):
            value = min(value, max_Value(game.result(state, action), alpha, beta))
        
            if (value <= alpha):
                return value
            beta = min(beta, value)
        return value

    nextMove = None
    maxVal = -float("inf")
    beta = float("inf")
    global nodeGenerated
    nodeGenerated = 1

    try:
        for action in game.legalMoves(state):
            value = max_Value(game.result(state, action), maxVal, beta)
            if value > maxVal:
                maxVal = value
                nextMove = action
        return nextMove, maxVal
    except:
        return None

# calculate the utility of the board
def calcBoardUnit(game, player):
    for curState, curBoard in sorted(boardDict.iteritems()):
        value = 0
        for i in range(8):
            for j in range(8):
                if (curBoard[i][j] == "0"):
                    continue
                elif (curBoard[i][j][0] == "S"):
                    value += int(curBoard[i][j][1]) * rowValue[7-i]
                else:
                    value -= int(curBoard[i][j][1]) * rowValue[i]

        if (player == "S"):
            utility[curState] = value
        else:
            utility[curState] = -value
    

# convert the index to the next move
def convertExp(index):
    tmp = stateDict[startState][index]
    tmp = tmp.split("#")[-1]
    result = rowLetter[7-int(tmp[2])] + str(int(tmp[5])+1) + "-" + rowLetter[7-int(tmp[10])] + str(int(tmp[13])+1);
    return result


# Main function
if __name__ == '__main__':
  
    # read the data from input.txt
    player1 = f_in.readline().rstrip()
    player1 = player1[0]
    player2 = ""

    if (player1 == "S"):
        player2 = "C"
    else:
        player2 = "S"

    algorithm = f_in.readline().rstrip()
    maxDepth = int(f_in.readline().rstrip())

    board = [["0"] * 8 for i in range(8)]
    for i in range(8):
        tmp = f_in.readline().rstrip()
        tmp = tmp.split(",")
        for j in range(8):
            board[i][j] = tmp[j]

    rowValue = [int(i) for i in f_in.readline().rstrip().split(",")]

 
    startState = "root"
    boardDict[startState] = board
    stateQueue = []
    stateQueue.append(startState)

    curDepth = 0
    # global nodeGenerated
    nodeGenerated = 1
    nodePass = 0


    # generate all the states whthin maxDepth
    while stateQueue:
        curState = stateQueue.pop(0)
        curDepth = len(curState.split("#"))-1

        if(curDepth >= maxDepth):
            break

        stateDict[curState] = dict()
        curBoard = boardDict[curState]

        curPlayer = ""
        nextPlayer = ""

        if (curDepth % 2 == 0):
            curPlayer = player1
            nextPlayer = player2
        else:
            curPlayer = player2
            nextPlayer = player1

        hasLegalMove = False

        cur_board = Board(curBoard)
        legalMoves = cur_board.calcLegalMoves(curPlayer)
        # print legalMoves

        if legalMoves:
            nodePass = 0
            hasLegalMove = True
            for i in legalMoves:
                index = (i[0][0] * 8 + i[0][1], i[1][0] * 8 + i[1][1])     
                newState = curState + "#" + str(i)
                newBoard = cur_board.modifyBoard(curPlayer, i)

                boardDict[newState] = newBoard
                stateDict[curState][index] = newState
                stateQueue.append(newState)
                nodeGenerated += 1

        if not hasLegalMove and not cur_board.terminalTest(nodePass):
            nodePass += 1
            newState = curState + "#pass"
            newBoard = curBoard

            boardDict[newState] = newBoard
            stateDict[curState]["pass"] = newState
            stateQueue.append(newState)
            nodeGenerated += 1
    
    game = myGame()
    calcBoardUnit(game, player1)


    if (algorithm == "MINIMAX"):
        result = minimaxDecision("root", game)
        
    else:
        result = alphaBeta("root", game)
        
    
    if (result != None):
        nextMove = result[0]
        maxVal = result[1]


    nextState = stateDict[startState][nextMove]
    if (nextMove != "pass"):
        nextMove = convertExp(nextMove)
    nextValue = utility[nextState]

    print "nextMove:",nextMove
    print "nextValue:",nextValue
    print "maxVal:",maxVal
    print "nodeGenerated:",nodeGenerated

    try:
        with open("output.txt", "w") as f_out:
            f_out.write(str(nextMove) + "\n")
            f_out.write(str(nextValue) + "\n")
            f_out.write(str(maxVal) + "\n")
            f_out.write(str(nodeGenerated))
        f_in.close()
    except:
        pass
    