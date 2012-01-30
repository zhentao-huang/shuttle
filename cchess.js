function cchessman(id, type)
{
    this.id =id
    this.type = type
    this.alive = true
}

function picmap()
{
    this.pmap = {
        "bju" : "bju.png",
        "bma" : "bma.png",
        "bxiang" : "bxiang.png",
        "bshi" : "bshi.png",
        "bjiang" : "bjiang.png",
        "bpao" : "bpao.png",
        "bzhu" : "bzhu.png",

        "rbing" : "rbing.png",
        "rpao" : "rpao.png",
        "rju" : "rju.png",
        "rma" : "rma.png",
        "rxiang" : "rxiang.png",
        "rshi" : "rshi.png",
        "rshuai" : "rshuai.png"
    }

    this.get = function(type)
    {
        return this.pmap[type];
    }
}

function composition()
{
    this.ps= new Array(10)
    this.cms = new Object()
    for (var i=0; i<this.ps.length; ++i)
    {
        this.ps[i] = new Array(9)
    }

    this.deployChessman = function (type, x, y)
    {
        var ts = this.cms[type]
        if (ts == null)
        {
            ts = new Array()
            this.cms[type] = ts
        }

        var id = ts.length
        var cm = new cchessman(id, type)
        this.ps[y][x] = cm
        ts[id] = cm
        cm.x = x
        cm.y = y
    }

    this.deploy = function (map)
    {
        for (var p in map)
        {
            curx = map[p].x
            for (var cm in map[p].cms)
            {
                this.deployChessman(map[p].cms[cm], curx, map[p].y)
                if (map[p].s != undefined)
                {
                    curx += map[p].s
                }
                else
                {
                    ++curx
                }
            }
        }
    }

    this.begin = function()
    {
        var map = [
        { x:0 ,y:0, cms:["bju", "bma", "bxiang", "bshi", "bjiang", "bshi", "bxiang", "bma", "bju"]},
        { x:1 ,y:2, s:6, cms:["bpao", "bpao"]},
        { x:0 ,y:3, s:2, cms:["bzhu", "bzhu", "bzhu", "bzhu", "bzhu"]},
        { x:0 ,y:6, s:2, cms:["rbing", "rbing", "rbing", "rbing", "rbing"]},
        { x:1 ,y:7, s:6, cms:["rpao", "rpao"]},
        { x:0 ,y:9, cms:["rju", "rma", "rxiang", "rshi", "rshuai", "rshi", "rxiang", "rma", "rju"]}
        ]

        this.deploy(map)
    }
}

function board(pic)
{
    this.pic = pic

    this.setOrigXnY = function(origX, origY)
    {
        this.origX = origX
        this.origY = origY
        return this
    }

    this.setGapXnY = function(gapX, gapY)
    {
        this.gapX = gapX
        this.gapY = gapY
        return this
    }

    this.setChessmanWnH = function(cmW, cmH)
    {
        this.cmW = cmW
        this.cmH = cmH
        return this
    }

    this.setBoardSize = function(width, height)
    {
        this.bW = width
        this.bH = height
        return this
    }

    this.setRealSize = function(width, height)
    {
        this.rW = width
        this.rH = height
        return this
    }

    this.setupRatio = function(context, imageWidth, imageHeight)
    {
        this.setBoardSize(imageWidth, imageHeight)
        this.ratio = imageWidth/imageHeight;
        var clientWidth = context.canvas.clientWidth
        var height = clientWidth / this.ratio;
        this.scale = imageWidth / clientWidth;
        context.canvas.setAttribute("style","height:" + height + "px");
        this.setRealSize(clientWidth, height);
    }
}

function chessmatch(comp, pmap, bd, aw)
{
    this.comp = comp
    this.pmap = pmap
    this.bd = bd
    this.aw = aw

    this.setContext = function(context)
    {
        this.context = context
    }

    this.drawBoard = function(context)
    {
        this.bg = new Image()
        this.bg.src = this.bd.pic
        this.bg.draggable = false;
        this.bg.onload = function()
        {
            // Initial for match.bd
            context.canvas.width = this.width;
            context.canvas.height = this.height;
            match.bd.setupRatio(context, this.width, this.height)

            context.drawImage(this, 0, 0, this.width, this.height)
            context.fillText("" + match.bd.ratio+";" + match.bd.rH, 20,90) 
        }
    }

    this.drawChessmans = function(context)
    {
        for (var i=0; i<this.comp.ps.length; ++i)
        {
            var row = this.comp.ps[i];
            for (var j=0; j<row.length; ++j)
            {
                var cm = row[j]
                if (cm != undefined)
                {
                    this.drawChessman(context, cm, j, i)
                }
            }
        }
    }

    this.drawChessman = function(context, cm, x, y)
    {
        if (cm.alive)
        {
            var cimg = new Image()
            cimg.draggable = true;
            cimg.src = this.pmap.get(cm.type)
            cimg.width = this.bd.cmW
            cimg.height = this.bd.cmH


            var centX = this.bd.origX + this.bd.gapX * x
            var centY = this.bd.origY + this.bd.gapY * y

            var x = centX - this.bd.cmW/2
            var y = centY - this.bd.cmH/2

            cimg.onload = function()
            {
                context.drawImage(this, x, y, cimg.width, cimg.height);
            }

            match.aw.setPointDownHandler(cimg, function(ev)
            {
                var p = match.aw.getEventXnY(ev);
            })

            cm.img = cimg;
        }
    }

    this.getImageSize = function()
    {
        return {x:this.bg.width, y:this.bg.height};
    }

    this.getChessmanByXnY = function(offx, offy)
    {
        var bx = offx * this.bd.scale
        var by = offy * this.bd.scale

        var ix = (bx - this.bd.origX)/this.bd.gapX
        var iy = (by - this.bd.origY)/this.bd.gapY

        ix = Math.round(ix);
        iy = Math.round(iy);

        if (ix < 0) ix = 0
        if (ix > 8) ix = 8
        if (iy < 0) iy = 0
        if (iy > 9) iy = 9

        px = this.bd.origX + this.bd.gapX * ix
        py = this.bd.origY + this.bd.gapY * iy

        if ((px-offx)*(px-offx) + (py-offy)*(py-offy) <= (this.bg.cW/2)^2)
        {
            return this.comp.ps[iy][ix];
        }
        return undefined;
    }
}

var pos = 20;

function pointDownHandler(ev)
{
    var mXY = match.aw.getEventXnY(ev);

    var cm = match.getChessmanByXnY(mXY.x, mXY.y)
    
    if (cm)
    {
        var txt = "" + cm.type + cm.id;
        var w = match.context.measureText(txt)
        match.context.fillText(txt, pos, 100, w)
        pos += w.width
    }
    else
    {
        match.context.fillText("cm = " + cm, 20, 110);
    }
}

function ActionWrapper(obj)
{
    this.setPointDownHandler = function(obj, func)
    {
        obj.addEventListener("mousedown", func, false);
        obj.onmousedown = func;
        obj.ontouchstart = func;
    }

    this.getEventXnY = function(ev)
    {
        var mX = ev.layerX;
        if (!mX)
        {
            mX = ev.x;
        }

        if (!mX)
        {
            mX = ev.touches[0].clientX
            mY = ev.touches[0].clientY
            return {x:mX, y:mY}
        }

        var mY = ev.layerY;
        if (!mY)
        {
            mY = ev.y;
        }

        return {x:mX, y:mY}
    }
}

var match;

function start()
{
    var pmap = new picmap();
    var comp = new composition();
    comp.begin();
    var bd = new board("board.png") 
        .setOrigXnY(40, 40)         
        .setGapXnY(59, 58)          
        .setChessmanWnH(55, 55);

    var aw = new ActionWrapper();
    match = new chessmatch(comp, pmap, bd, aw);

    var canvas = $("#canvas")[0];
    var context = canvas.getContext("2d");
    match.setContext(context)
    /*
    document.ondragover = function(e){e.preventDefault();}
    document.ondragdrop = function(e){e.preventDefault();}
    document.onmouseover = function(e){e.preventDefault();}


    canvas.ondragover = function(e){e.preventDefault();}
    canvas.ondragdrop = function(e){e.preventDefault();}
*/
    aw.setPointDownHandler(canvas, pointDownHandler);

    match.drawBoard(context)
    match.drawChessmans(context)
}
