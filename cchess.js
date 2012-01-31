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
    this.bgColor = "#f3bf6c"

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
            context.fillstyle = this.bgColor
            context.fill()

            context.drawImage(this, 0, 0, this.width + 1, this.height + 1)
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
            var width = this.bd.cmW
            var height = this.bd.cmH


            var centX = this.bd.origX + this.bd.gapX * x
            var centY = this.bd.origY + this.bd.gapY * y

            var x = centX - this.bd.cmW/2
            var y = centY - this.bd.cmH/2

            cimg.onload = function()
            {
                cm.savedImg = context.getImageData(x, y, width + 2, height + 2);
                cm.savedPos = {"x":x,"y":y,"w":width,"h":height};
                context.drawImage(this, x, y, width, height);
            }

            cm.img = cimg;
        }
    }

    this.getImageSize = function()
    {
        return {x:this.bg.width, y:this.bg.height};
    }

    this.setupMovingChessman = function(pos, bx, by)
    {
        if ((pos.px-bx)*(pos.px-bx) + (pos.py-by)*(pos.py-by) <= (this.bd.cmW/2)*(this.bd.cmW/2))
        {
            var cm = this.comp.ps[pos.iy][pos.ix];
            if (cm)
            {
                var x = pos.px - this.bd.cmW/2;
                var y = pos.py - this.bd.cmH/2;
                cm.pointOff = {"x": bx - x,"y": by - y}
                this.dcm = cm
            }
        }
    }

    this.attractChessman = function()
    {
        if (this.dcm)
        {
            this.dcm.pointOff = {"x": 0, "y": 0}
        }
    }

    this.getChessmanPosByXnY = function(bx, by)
    {
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

        return {"ix":ix,"iy":iy,"px":px,"py":py}
    }

    this.deploy = function(cm, x, y)
    {
        this.comp.ps[cm.y][cm.x] = undefined
        this.comp.ps[y][x] = cm
        cm.x = x
        cm.y = y
    }

    this.takeChessman = function(cm)
    {
        cm.alive = false;
        this.context.putImageData(this.dcm.savedImg, this.dcm.savedPos.x, this.dcm.savedPos.y);
        this.context.putImageData(cm.savedImg, cm.savedPos.x, cm.savedPos.y);
        this.dcm.savedImg = this.context.getImageData(this.dcm.savedPos.x, this.dcm.savedPos.y, this.dcm.savedPos.w + 2, this.dcm.savedPos.h + 2)
    }

    this.convert = function(off)
    {
        return off * this.bd.scale
    }

    this.moveChessman = function(x, y)
    {
        var cm = this.dcm
        this.context.putImageData(cm.savedImg, cm.savedPos.x, cm.savedPos.y);
        cm.savedPos.x = x - cm.pointOff.x
        cm.savedPos.y = y - cm.pointOff.y
        cm.savedImg = this.context.getImageData(cm.savedPos.x, cm.savedPos.y, cm.savedPos.w + 2, cm.savedPos.h + 2);
        this.context.drawImage(cm.img, cm.savedPos.x, cm.savedPos.y, cm.savedPos.w, cm.savedPos.h);
    }

    this.pickupChessman = function(x, y)
    {
        var pos = this.getChessmanPosByXnY(x, y);
        this.setupMovingChessman(pos, x, y)
    }

    this.putdownChessman = function(bx, by)
    {
        var pos = match.getChessmanPosByXnY(bx, by);
        
        var prev = this.comp.ps[pos.iy][pos.ix]
        if (prev)
        {
            this.takeChessman(prev)
        }

        var x = pos.px - match.bd.cmW/2
        var y = pos.py - match.bd.cmH/2

        match.attractChessman();
        match.moveChessman(x, y);
        match.deploy(match.dcm, pos.ix, pos.iy);
        match.dcm = undefined;
    }

    this.isMoving = function()
    {
        return this.dcm
    }
}

var pos = 20;
var line = 90;
var gap = 20

function pointDownHandler(ev)
{
    var mXY = match.aw.getEventXnY(ev);

    var x = match.convert(mXY.x)
    var y = match.convert(mXY.y)

    match.pickupChessman(x, y)
}

function pointMoveHandler(ev)
{
    if (match.isMoving())
    {
        var mXY = match.aw.getEventXnY(ev);
        var x = match.convert(mXY.x);
        var y = match.convert(mXY.y);
        match.moveChessman(x, y);
    }
}

function log(txt)
{
    var m = match.context.measureText(txt);
    match.context.fillText(txt, pos, line);
    line += gap
}

function pointUpHandler(ev)
{
    if (match.isMoving())
    {
        var mXY = match.aw.getEventXnY(ev);
        var x = match.convert(mXY.x);
        var y = match.convert(mXY.y);
        
        match.putdownChessman(x, y);
    }
}

function ActionWrapper(obj)
{
    this.setPointDownHandler = function(obj, func)
    {
        obj.addEventListener("mousedown", func, false);
        obj.addEventListener("touchstart", func, false);
    }

    this.setPointMoveHandler = function(obj, func)
    {
        obj.addEventListener("mousemove", func, false);
        obj.addEventListener("touchmove", func, false);
    }

    this.setPointUpHandler = function(obj, func)
    {
        obj.addEventListener("mouseup", func, false);
        obj.addEventListener("touchend", func, false);
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
            if (ev.touches && ev.touches.length > 0)
            {
                mX = ev.touches[0].clientX
                mY = ev.touches[0].clientY
                return {x:mX, y:mY}
            }
            if (ev.changedTouches && ev.changedTouches.length > 0)
            {
                mX = ev.changedTouches[0].clientX;
                mY = ev.changedTouches[0].clientY;
                return {x:mX, y:mY}
            }
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

function nodefault(ev)
{
    ev.preventDefault()
}

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

    document.addEventListener("touchmove", nodefault, false);
    document.addEventListener("touchend", nodefault, false);
    document.addEventListener("touchstart", nodefault, false);

    aw.setPointDownHandler(canvas, pointDownHandler);
    aw.setPointMoveHandler(canvas, pointMoveHandler);
    aw.setPointUpHandler(canvas, pointUpHandler);

    match.drawBoard(context)
    match.drawChessmans(context)
}
