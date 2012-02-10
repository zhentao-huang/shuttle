function perform(match, comm)
{
    var set = new game(match, comm)
    set.launch()
    return set
}

function communicator(comm, role)
{
    this.comm = comm + "?role=" + role + "&"
    this.role = role

    this.register = function(action)
    {
        var url = this.comm + "action=register&rand=" + Math.random();
        jQuery.get(url, action);
    }

    this.send = function(action, text)
    {
        var url = this.comm + "action=send&content=" + text + "&rand=" + Math.random()
        jQuery.get(url, action);
    }

    this.unregister = function(action)
    {
        var url = this.comm + "action=unregister&rand=" + Math.random()
        jQuery.get(url, action);
    }

    this.receive = function(action, timeout)
    {
        var url;
        if (timeout === undefined)
        {
            url = this.comm + "action=recv&rand=" + Math.random();
        }
        else
        {
            url = this.comm + "action=recv&timeout=" + timeout + "&rand=" + Math.random()
        }
        jQuery.get(url, action);
    }

}

function game(match, comm)
{
    this.match = match
    this.comm = new communicator(comm, this.match.getMySide())
    this.timeout = 60

    this.launch = function()
    {
        this.comm.unregister(this.callback("sayHello"));
    }

    this.sayHello = function(data)
    {
        this.comm.register(this.callback("ack"));
    }

    this.ack = function(data)
    {
        this.comm.receive(this.callback("watch"), this.timeout);
    }

    this.ok = function()
    {

    }

    this.watch = function(data)
    {
        var obj
        try
        {
            eval("obj = " + data);
        }
        catch(err)
        {
        }
        
        if (obj && obj.content)
        {
            if (obj.content === "hello")
            {
                this.comm.receive(this.callback("watch"), this.timeout);
            }
            /*
            else if (obj.content === "yell")
            {
                yell()
                this.comm.receive(this.callback("watch"), this.timeout);
            }
            */
            else
            {
                var turn = obj.content
                turn.isMine = false
                this.match.performMove(turn)
                this.comm.receive(this.callback("watch"), this.timeout); 
            }
        }
        else
        {
            this.ack();
        }
    }

    this.performMyTurn = function()
    {
    }

    this.onestep = function(step)
    {
        if (step.isMine)
        {
            var str = JSON.stringify(step);
            this.comm.send(this.callback("ok"), str);
        }

        this.match.play.toggleTurn();
        updateTitle()
    }

    this.match.setTurnHandler(this.callback("onestep"));
}

applyCallback(game)
