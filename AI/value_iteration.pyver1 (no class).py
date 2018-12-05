import numpy as np
import time

f_in = open("input5.txt", "r");

grid_world = []
prev_world = []
is_terminal = []
is_block = []
policy = []
rewards = []

max_change = float('inf')

n_row = 0
n_column = 0

pro_walk = 0
pro_run = 0
pro_walk_other = 0
pro_run_other = 0

reward = []
discount = 0

epsilon = 0


def walkup(i, j):
    if i == 0 or is_block[i-1][j] == 1:
        return grid_world[i][j]
    return grid_world[i-1][j]

def walkdown(i, j):
    if i == n_row-1 or is_block[i+1][j] == 1:
        return grid_world[i][j]
    return grid_world[i+1][j]

def walkleft(i, j):
    if j == 0 or is_block[i][j-1] == 1:
        return grid_world[i][j]
    return grid_world[i][j-1]

def walkright(i, j):
    if j == n_column-1 or is_block[i][j+1] == 1:
        return grid_world[i][j]
    return grid_world[i][j+1]

def runup(i, j):
    if i <= 1 or is_block[i-1][j] == 1 or is_block[i-2][j] == 1:
        return grid_world[i][j]
    return grid_world[i-2][j]

def rundown(i, j):
    if i >= n_row-2 or is_block[i+1][j] == 1 or is_block[i+2][j] == 1:
        return grid_world[i][j]
    return grid_world[i+2][j]

def runleft(i, j):
    if j <= 1 or is_block[i][j-1] == 1 or is_block[i][j-2] == 1:
        return grid_world[i][j]
    return grid_world[i][j-2]

def runright(i, j):
    if j >= n_column-2 or is_block[i][j+1] == 1 or is_block[i][j+2] == 1:
        return grid_world[i][j]
    return grid_world[i][j+2]

def update(i, j):
    global max_change

    if is_terminal[i,j] == 1 or is_block[i,j] == 1:    
        return

    max_pv = 0 

    # walk_up = grid_world[i][j] if (i == 0 or is_block[i-1][j] == 1) else grid_world[i-1][j]
    # walk_down = grid_world[i][j] if (i == n_row-1 or is_block[i+1][j] == 1) else grid_world[i+1][j]
    # walk_left = grid_world[i][j] if (j == 0 or is_block[i][j-1] == 1) else grid_world[i][j-1]
    # walk_right = grid_world[i][j] if (j == n_column-1 or is_block[i][j+1] == 1) else grid_world[i][j+1]
    # run_up = grid_world[i][j] if (i <= 1 or is_block[i-1][j] == 1 or is_block[i-2][j] == 1) else grid_world[i-2][j]
    # run_down = grid_world[i][j] if (i >= n_row-2 or is_block[i+1][j] == 1 or is_block[i+2][j] == 1) else grid_world[i+2][j]
    # run_left = grid_world[i][j] if (j <= 1 or is_block[i][j-1] == 1 or is_block[i][j-2] == 1) else grid_world[i][j-2]
    # run_right = grid_world[i][j] if (j >= n_column-2 or is_block[i][j+1] == 1 or is_block[i][j+2] == 1) else grid_world[i][j+2]
    
    walk_up = walkup(i, j); walk_down = walkdown(i, j); walk_left = walkleft(i, j); walk_right = walkright(i, j);
    run_up = runup(i, j); run_down = rundown(i, j); run_left = runleft(i, j); run_right = runright(i, j);

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
    
    moves_directions = ['Walk Down', 'Walk Up', 'Walk Left', 'Walk Right', 'Run Down', 'Run Up', 'Run Left', 'Run Right']

    moves_pvs_max = moves_pvs * discount + rewards
    max_idx = np.argmax(moves_pvs_max)

    max_change = max ( abs(grid_world[i][j] - moves_pvs_max[max_idx]), max_change )

    grid_world[i][j] = moves_pvs_max[max_idx]

    plc = moves_directions[max_idx]
    policy[i][j] = plc


# Main function
if __name__ == '__main__':

    # read the data from input.txt
    tmp = [j for j in f_in.readline().rstrip().split(",")]
    tmp = map(eval, tmp)
    n_row = tmp[0]
    n_column = tmp[1]
    grid_world = np.zeros((n_row, n_column))

    is_terminal = np.zeros((n_row, n_column))
    is_block = np.zeros((n_row, n_column))

    policy = [['' for col in range(n_column)] for row in range(n_row)]

    wall_grid_num = int(f_in.readline().rstrip())
    wall_grid_pos = [[] for i in range(wall_grid_num)]
    for i in range(wall_grid_num):
        wall_grid_pos[i] = [int(j) for j in f_in.readline().rstrip().split(",")]
        is_block[wall_grid_pos[i][0]-1][wall_grid_pos[i][1]-1] = 1
        policy[wall_grid_pos[i][0]-1][wall_grid_pos[i][1]-1] = "None"
        
    terminal_state_num = int(f_in.readline().rstrip())
    terminal_state_pos = [[] for i in range(terminal_state_num)]
    terminal_state_rew = np.zeros((terminal_state_num)) 
    for i in range(terminal_state_num):
        tmp = [j for j in f_in.readline().rstrip().split(",")]
        tmp = map(eval, tmp)
        terminal_state_pos[i] = tmp[0: 2]
        terminal_state_rew[i] = tmp[-1]
        grid_world[terminal_state_pos[i][0]-1][terminal_state_pos[i][1]-1] = terminal_state_rew[i]
        is_terminal[terminal_state_pos[i][0]-1][terminal_state_pos[i][1]-1] = 1
        policy[terminal_state_pos[i][0]-1][terminal_state_pos[i][1]-1] = "Exit"

    max_terminal_id = np.argmax(terminal_state_rew)
    index_row = terminal_state_pos[i][0] - 1
    index_col = terminal_state_pos[i][1] - 1

    tmp = [j for j in f_in.readline().rstrip().split(",")]
    tmp = map(eval, tmp)
    pro_walk = tmp[0]
    pro_run = tmp[1]

    pro_walk_other = 0.5 * (1 - pro_walk)
    pro_run_other = 0.5 * (1 - pro_run)

    reward = [j for j in f_in.readline().rstrip().split(",")]
    reward = map(eval, reward)
    discount = float(f_in.readline().rstrip())
    rewards = np.array([reward[0],reward[0],reward[0],reward[0],reward[1],reward[1],reward[1],reward[1]])

    iter_cnt = 1

    # start = time.clock()
    # end = time.clock()

    # while (max_change > epsilon * (1-discount)/discount) and (end - start <= 160):
    while (max_change > epsilon * (1-discount)/discount):
        print 'iteration {}'.format(iter_cnt).center(72, '-')
        
        max_change = 0

        for i in range(index_row, n_row):
            for j in range(index_col, n_column):
                update(i, j)

        for i in range(index_row, n_row):
            for j in range(index_col-1, -1, -1):
                update(i, j)

        for i in range(index_row-1, -1, -1):
            for j in range(index_col, n_column):
                update(i, j)

        for i in range(index_row-1, -1, -1):
            for j in range(index_col-1, -1, -1):
                update(i, j)

        print(grid_world)

        # end = time.clock()
        # print "---------",end - start,"----------"
        iter_cnt += 1

    # for x in reversed(policy):
    #     print x

    try:
        with open("output.txt", "w") as f_out:
            for i in reversed(policy):
                for j in i[0:-1]:
                    f_out.write(str(j) + ",")
                for j in i[-1]:
                    f_out.write(str(j))
                f_out.write("\n")

        f_in.close()
    except:
        pass
    