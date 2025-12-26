#trying to optimize the hyperparameter learning rate, doing so by picking an initial value and comparing accuracy of the trained model for larger and smaller lrs. 
#this is based on the assumption that lr->testerror relationship resembles a parabula (i.e we have exactly one valley) this is how convergence and therefore 
#completeness is guaranteed.  
#Of course, this is only viable because the model is small and training does not take a lot of time.


from training import train


res = 0.001
up_step = res*2.0
down_step = res/2.0



def search(up_step, down_step):
    global res
    up_acc = train(lr = up_step)
    down_acc = train(lr = down_step)

    if(up_acc > down_acc):
        if(res == up_step):
            print(f"Optimal learning rate: {res}")
            return res      #we now know that optimal learning rate is in [up_step, up_step/2] here we assume that up_step is sufficiantly accurate - could run further search in this interval 
        res = up_step
        print(f"current:{res}")
        search(up_step*2, up_step/2)
    else:
        print(f"current:{res}")
        search(down_step*2, down_step/2)
    

search(up_step, down_step)
