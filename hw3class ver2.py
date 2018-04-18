import numpy as np
import copy
# np.set_printoptions(threshold=np.nan)

f_in = open("input2.txt", "r");

grid_world = []
n_row = 0
n_column = 0
pro_walk = 0
pro_run = 0
terminal_state_num = 0
terminal_state_pos = []
terminal_state_rew = []
reward = []
discount = 0
rewards = []

max_change = float('inf')
epsilon = float(0)

value = []
action = []

index_row = 0
index_col = 0

    
class Grid(object):
    def __init__(self):
        self.is_terminal = False
        self.policy = ''
        self.value = 0

class GridWorld(object):
    def __init__(self, array):
        grids = []
        for row_idx, row in enumerate(array):
            grids.append([])
            for col_idx, value in enumerate(row):
                grid = Grid()
                grid.row = row_idx
                grid.col = col_idx
                grid.value = value
                grid.reward = value or 0
                grid.blocks = np.isnan(value)
                grids[row_idx].append(grid)
            assert len(grids[row_idx]) == n_column
        assert len(grids) == n_row
        for i in range(terminal_state_num):
            grids[terminal_state_pos[i][0]-1][terminal_state_pos[i][1]-1].is_terminal = True
        self.grids = grids

    def policy(self):
        return [[grid.policy for grid in rows] for rows in self.grids]

    def __iter__(self):
        # for row in self.grids:
        #     for grid in row:
        #         yield grid

        for i in range(index_row, n_row):
            for j in range(index_col, n_column):
                yield self.grids[i][j]

        for i in range(index_row, n_row):
            for j in range(index_col-1, -1, -1):
                yield self.grids[i][j]

        for i in range(index_row-1, -1, -1):
            for j in range(index_col, n_column):
                yield self.grids[i][j]

        for i in range(index_row-1, -1, -1):
            for j in range(index_col-1, -1, -1):
                yield self.grids[i][j]

    def as_array(self):
        a = [ [grid.value for grid in row] for row in self.grids ]
        return np.array(a)

    def walkup(self, grid):
        row = grid.row; col = grid.col
        if row > 0:
            row -= 1
        else:
            return grid
        return self._next_grid_if_not_blocked(grid, row, col)

    def walkdown(self, grid):
        row = grid.row; col = grid.col
        if row < n_row-1:
            row += 1
        else:
            return grid
        return self._next_grid_if_not_blocked(grid, row, col)

    def walkleft(self, grid):
        row = grid.row; col = grid.col
        if col > 0:
            col -= 1
        else:
            return grid
        return self._next_grid_if_not_blocked(grid, row, col)

    def walkright(self, grid):
        row = grid.row; col = grid.col
        if col < n_column-1:
            col += 1
        else:
            return grid
        return self._next_grid_if_not_blocked(grid, row, col)

    def runup(self, grid):
        row = grid.row; col = grid.col
        if row > 1:
            mid = self.grids[row-1][col]
            if mid.blocks:
                return grid
            row -= 2
        else:
            return grid
        return self._next_grid_if_not_blocked(grid, row, col)

    def rundown(self, grid):
        row = grid.row; col = grid.col
        if row < n_row-2:
            mid = self.grids[row+1][col]
            if mid.blocks:
                return grid
            row += 2
        else:
            return grid
        return self._next_grid_if_not_blocked(grid, row, col)

    def runleft(self, grid):
        row = grid.row; col = grid.col
        if col > 1:
            mid = self.grids[row][col-1]
            if mid.blocks:
                return grid
            col -= 2
        else:
            return grid
        return self._next_grid_if_not_blocked(grid, row, col)

    def runright(self, grid):
        row = grid.row; col = grid.col
        if col < n_column-2:
            mid = self.grids[row][col+1]
            if mid.blocks:
                return grid
            col += 2
        else:
            return grid
        return self._next_grid_if_not_blocked(grid, row, col)

    def _next_grid_if_not_blocked(self, grid, row, col):
        n = self.grids[row][col]
        if n.blocks:
            return grid
        return n

class ValueIterationAlgo(object):
    def __init__(self, world):
        self.world = world

    def update(self, state):
        global max_change

        if state.is_terminal or state.blocks: return
        max_pv = 0 

        pro_walk_other = 0.5 * (1 - pro_walk)
        pro_run_other = 0.5 * (1 - pro_run)

        walk_up = self.world.walkup(state).value;       walk_down = self.world.walkdown(state).value; 
        walk_left = self.world.walkleft(state).value;   walk_right = self.world.walkright(state).value;
        run_up = self.world.runup(state).value;         run_down = self.world.rundown(state).value;
        run_left = self.world.runleft(state).value;     run_right = self.world.runright(state).value;


        moves_pvs = np.array([
            pro_walk * walk_up + pro_walk_other * walk_left + pro_walk_other * walk_right,
            pro_walk * walk_down + pro_walk_other * walk_left + pro_walk_other * walk_right,
            pro_walk * walk_left + pro_walk_other * walk_up + pro_walk_other * walk_down,
            pro_walk * walk_right + pro_walk_other * walk_up + pro_walk_other * walk_down,
            pro_run * run_up + pro_run_other * run_left + pro_run_other * run_right,
            pro_run * run_down + pro_run_other * run_left + pro_run_other * run_right,
            pro_run * run_left + pro_run_other * run_up + pro_run_other * run_down,
            pro_run * run_right + pro_run_other * run_up + pro_run_other * run_down
        ])

        
        # moves_directions = ['Walk Up', 'Walk Down', 'Walk Left', 'Walk Right', 'Run Up', 'Run Down', 'Run Left', 'Run Right']
        moves_directions = ['Walk Down', 'Walk Up', 'Walk Left', 'Walk Right', 'Run Down', 'Run Up', 'Run Left', 'Run Right']

        moves_pvs_max = moves_pvs * discount + rewards

        max_idx = np.argmax(moves_pvs_max)
        max_pv = moves_pvs_max[max_idx]
        policy = moves_directions[max_idx]


        max_change = max ( abs(state.value - max_pv), max_change )
        state.value = max_pv
        state.policy = policy


# Main function
if __name__ == '__main__':

    # read the data from input.txt
    tmp = [j for j in f_in.readline().rstrip().split(",")]
    tmp = map(eval, tmp)
    n_row = tmp[0]
    n_column = tmp[1]
    grid_world = np.zeros((n_row, n_column))

    wall_grid_num = int(f_in.readline().rstrip())
    wall_grid_pos = [[] for i in range(wall_grid_num)]
    for i in range(wall_grid_num):
        wall_grid_pos[i] = [int(j) for j in f_in.readline().rstrip().split(",")]
        grid_world[wall_grid_pos[i][0]-1][wall_grid_pos[i][1]-1] = np.nan
        
    terminal_state_num = int(f_in.readline().rstrip())
    terminal_state_pos = [[] for i in range(terminal_state_num)]
    terminal_state_rew = [[] for i in range(terminal_state_num)]
    for i in range(terminal_state_num):
        tmp = [j for j in f_in.readline().rstrip().split(",")]
        tmp = map(eval, tmp)
        terminal_state_pos[i] = tmp[0: 2]
        terminal_state_rew[i] = tmp[-1]
        grid_world[terminal_state_pos[i][0]-1][terminal_state_pos[i][1]-1] = terminal_state_rew[i]

    max_terminal_id = np.argmax(terminal_state_rew)
    index_row = terminal_state_pos[i][0] - 1
    index_col = terminal_state_pos[i][1] - 1

    tmp = [j for j in f_in.readline().rstrip().split(",")]
    tmp = map(eval, tmp)
    pro_walk = tmp[0]
    pro_run = tmp[1]

    reward = [j for j in f_in.readline().rstrip().split(",")]
    reward = map(eval, reward)
    discount = float(f_in.readline().rstrip())

    rewards = np.array([reward[0],reward[0],reward[0],reward[0],reward[1],reward[1],reward[1],reward[1]])

    global world
    world = GridWorld(grid_world)
    # prev_world = np.ones_like(grid_world) # init to something other than input
    algo = ValueIterationAlgo(world);
    iter_cnt = 0

    while (max_change > epsilon * (1-discount)/discount):
        print 'iteration {}'.format(iter_cnt).center(72, '-')

        max_change = 0

        for grid in world:
            algo.update(grid)
        iter_cnt += 1


    result = world.policy()
    for i in range(wall_grid_num):
        result[wall_grid_pos[i][0]-1][wall_grid_pos[i][1]-1] = "None"
    for i in range(terminal_state_num):
        result[terminal_state_pos[i][0]-1][terminal_state_pos[i][1]-1] = "Exit"

    # for x in reversed(result):  
    #     print x

    try:
        with open("output.txt", "w") as f_out:
            for i in reversed(result):
                for j in i[0:-1]:
                    f_out.write(str(j) + ",")
                for j in i[-1]:
                    f_out.write(str(j))
                f_out.write("\n")

        f_in.close()
    except:
        pass
    