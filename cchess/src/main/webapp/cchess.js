// Define a chessman
// This object is a combination of chessman's attributes
//    id    : integer, the index of the chessman in composition.cms array
//    type  : string, the first char is "r"(red) or "b"(black), the
//            following are the name of the chess. Type is also an index
//            map to picture of chessman
//    alive : is the cchess is still alive
function cchessman(id, type)
{
    this.id =id
    this.type = type
    this.alive = true
} // end of function cchessman

// Define a map of chessman.type to picture
//    pmap      : a map of data
//    get(type) : Get a picture file by specified type
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
} // end of function pic map

// Define a composition of chessmans, setup chessmans' position in the board
//    ps            : the 2D array of chessmans
//    cms           : the map of chassmans, you can get any array of a type of chessman
function composition(side)
{
    this.ps= new Array(10)
    this.cms = new Object()
    this.side = side
    for (var i=0; i<this.ps.length; ++i)
    {
        this.ps[i] = new Array(9)
    }

    this.getUiPos = function(x,y)
    {
        if (this.side === "black")
        {
            x = 8 - x;
            y = 9 - y;
        }
        return {x:x, y:y}
    }

    this.deployChessman = function (type, x, y)
    {
        var ts = this.cms[type]
        if (ts == null)
        {
            ts = new Array()
            this.cms[type] = ts
        }

        var pos = this.getUiPos(x, y)
        x = pos.x
        y = pos.y

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

    // Begin deploy chessman, with or without a specified map of 
    // chessmans's location
    this.begin = function(map)
    {
        if (!map)
        {
            map = [
            { x:0 ,y:0, cms:["bju", "bma", "bxiang", "bshi", "bjiang", "bshi", "bxiang", "bma", "bju"]},
            { x:1 ,y:2, s:6, cms:["bpao", "bpao"]},
            { x:0 ,y:3, s:2, cms:["bzhu", "bzhu", "bzhu", "bzhu", "bzhu"]},
            { x:0 ,y:6, s:2, cms:["rbing", "rbing", "rbing", "rbing", "rbing"]},
            { x:1 ,y:7, s:6, cms:["rpao", "rpao"]},
            { x:0 ,y:9, cms:["rju", "rma", "rxiang", "rshi", "rshuai", "rshi", "rxiang", "rma", "rju"]}
            ]
        }

        this.deploy(map)
    }

} //end of composition

// Define a board, keep properties of a picture of board
//   origX, origY : the offset of left top corner of the board
//   gapX , gapY  : the gap between points of the board
//   cmW  , cmH   : the preferred chessman's width and height
//   bW   , bH    : the width and height of board
//   rW   , rH    : the real size of width and height in a screen
//   ratio        : the scale of width and height of the board
//   scale        : the scale of zoom of image original size and real
//                  device screen size
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
} // end of function board

// Define a chess match
// This object define a combination of all match stuffs
//    comp     : composition of the match
//    pmap     : picmap
//    bd       : board
//    aw       : action wrapper
//    bgColor  : background color, should be move to bd!!
//    play     : rule and turn's check
//    context  : 2d context of canvas
//    dcm      : current moving chessman
//
function chessmatch(comp, pmap, bd, aw)
{
    this.comp = comp
    this.pmap = pmap
    this.bd = bd
    this.aw = aw
    this.bgColor = "#f3bf6c"

    this.play = new cchessplay(comp);

    this.setContext = function(context)
    {
        this.context = context
    }

    this.drawBoard = function(context)
    {
        this.bg = new Image()
        this.bg.src = this.bd.pic
        this.bg.draggable = false;
        var match = this
        this.bg.onload = function()
        {
            // Initial for match.bd
            context.canvas.width = this.width;
            context.canvas.height = this.height;
            match.bd.setupRatio(context, this.width, this.height)
            context.fillstyle = this.bgColor
            context.fill()

            context.drawImage(this, 0, 0, this.width + 1, this.height + 1)

            match.drawChessmans(context)
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

            cimg.addEventListener("load", 
            function()
            {
                cm.savedImg = context.getImageData(x, y, width + 2, height + 2);
                cm.savedPos = {"x":x,"y":y,"w":width,"h":height};

                context.drawImage(this, x, y, width, height);
            },
            false);

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
            this.pickup(pos)
            if ( this.dcm && this.play.isMyTurn() && this.play.isMyChess(this.dcm))
            {
                var x = pos.px - this.bd.cmW/2;
                var y = pos.py - this.bd.cmH/2;
                this.dcm.pointOff = {"x": bx - x,"y": by - y}
            }
            else
            {
                this.dcm = undefined;
            }
        }
    }

    this.pickup = function(pos)
    {
        var cm = this.comp.ps[pos.iy][pos.ix];
        if (cm)
        {
            cm.pointOff = {"x": 0,"y": 0}
            this.dcm = cm
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

        return this.getChessmanPosByPoint(ix, iy)
    }

    this.getChessmanPosByPoint = function(ix, iy)
    {
        px = this.bd.origX + this.bd.gapX * ix
        py = this.bd.origY + this.bd.gapY * iy

        return {"ix":ix,"iy":iy,"px":px,"py":py}
    }

    this.place = function(cm, x, y)
    {
        if (cm.x === x && cm.y === y)
        {
            return false;
        }
        var isMine = this.play.isMyChess(cm)
        ret = {oldX:cm.x,oldY:cm.y,newX:x, newY:y, isMine:isMine}
        this.comp.ps[cm.y][cm.x] = undefined
        this.comp.ps[y][x] = cm
        cm.x = x
        cm.y = y

        return ret
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

    this.rotate = function(x, y)
    {
        return {x:8-x, y:9-y}
    }

    this.moveChessman = function(x, y)
    {
        var cm = this.dcm
        this.context.putImageData(cm.savedImg, cm.savedPos.x, cm.savedPos.y);
        cm.savedPos.x = x - cm.pointOff.x
        cm.savedPos.y = y - cm.pointOff.y
        
        if (cm.savedPos.x < 0)
        {
            cm.savedPos.x = 0
        }

        if (cm.savedPos.y < 0)
        {
            cm.savedPos.y = 0
        }

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
        var pos = this.getChessmanPosByXnY(bx, by);
        if (!this.play.isMoveable(this.dcm, pos.ix, pos.iy))
        {
            pos = this.getChessmanPosByPoint(this.dcm.x, this.dcm.y);
        }
        this.putdown(pos)
    }

    this.putdown = function(pos)
    {
        var prev = this.comp.ps[pos.iy][pos.ix]
        if (prev)
        {
            this.takeChessman(prev)
        }

        var x = pos.px - this.bd.cmW/2
        var y = pos.py - this.bd.cmH/2

        this.attractChessman();
        this.moveChessman(x, y);

        var ret = this.place(this.dcm, pos.ix, pos.iy);

        if (ret)
        {
            this.emitTurnEvent(ret);
        }

        this.dcm = undefined;
    }

    this.isMoving = function()
    {
        return this.dcm
    }

    this.setTurnHandler = function(callback)
    {
        this.turnHandler = callback;
    }

    this.emitTurnEvent = function(obj)
    {
        if (this.turnHandler)
        {
            this.turnHandler.call(this, obj)
        }
    }

    this.getMySide = function()
    {
        return this.comp.side;
    }

    this.performMove = function(turn)
    {
        oldpos = this.rotate(turn.oldX, turn.oldY)
        newpos = this.rotate(turn.newX, turn.newY)

        oldpos = this.getChessmanPosByPoint(oldpos.x, oldpos.y)
        newpos = this.getChessmanPosByPoint(newpos.x, newpos.y)

        this.pickup(oldpos)
        this.putdown(newpos) 
    }

    this.pointDownHandler = function(ev)
    {
        if (this.play.isMyTurn())
        {
            var mXY = this.aw.getEventXnY(ev);

            var x = this.convert(mXY.x)
            var y = this.convert(mXY.y)

            this.pickupChessman(x, y)
        }
    }

    this.pointMoveHandler = function(ev)
    {
        if (this.isMoving())
        {
            var mXY = this.aw.getEventXnY(ev);
            var x = this.convert(mXY.x);
            var y = this.convert(mXY.y);
            this.moveChessman(x, y);
        }
    }

    this.pointUpHandler = function(ev)
    {
        if (this.isMoving())
        {
            var mXY = this.aw.getEventXnY(ev);
            var x = this.convert(mXY.x);
            var y = this.convert(mXY.y);
            
            this.putdownChessman(x, y);
        }
    }

    this.enableEventHandlers = function()
    {
        document.addEventListener("touchmove", nodefault, false);
        document.addEventListener("touchend", nodefault, false);
        document.addEventListener("touchstart", nodefault, false);

        var canvas = this.context.canvas
        this.aw.setPointDownHandler(canvas, this.callback("pointDownHandler"));
        this.aw.setPointMoveHandler(canvas, this.callback("pointMoveHandler"));
        this.aw.setPointUpHandler(canvas, this.callback("pointUpHandler"));
    }
} // end of function chessmanmatch

// Add callback function to chessmatch object
applyCallback(chessmatch)

var pos = 20;
var line = 90;
var gap = 20

function log(txt)
{
    var m = match.context.measureText(txt);
    match.context.fillText(txt, pos, line);
    line += gap
}

// Define a wrapper to enable over mobile device browser and
// Chrome browser acton well and similar
//    getEventXnY(event) : get point position from event object
function EventWrapper()
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
        var mX = ev.offsetX
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

        var mY = ev.offsetY
        if (!mY)
        {
            mY = ev.y;
        }

        return {x:mX, y:mY}
    }
}  // end of function EventWrapper;

function nodefault(ev)
{
    ev.preventDefault()
}  // end of function nodefault

function startGame(side, map)
{
    var pmap = new picmap();
    var comp = new composition(side);
    comp.begin(map);
    var bd = new board("board.png") 
        .setOrigXnY(40, 40)         
        .setGapXnY(59, 58)          
        .setChessmanWnH(55, 55);

    var aw = new EventWrapper();
    match = new chessmatch(comp, pmap, bd, aw);

    var canvas = $("#canvas")[0];
    var context = canvas.getContext("2d");
    match.setContext(context)
    match.enableEventHandlers()

    match.drawBoard(context)

    return match
}  // end of function startGame
