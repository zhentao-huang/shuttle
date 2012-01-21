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
        "rshuai" : "rshuai.png",
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

function board(pic, origX, origY, gapX, gapY, cmW, cmH)
{
    this.pic = pic
    this.origX = origX
    this.origY = origY
    this.gapX = gapX
    this.gapY = gapY
    this.cmW = cmW
    this.cmH = cmH
}

function chessmatch(comp, pmap, bd)
{
    this.comp = comp
    this.pmap = pmap
    this.bd = bd

    this.drawBoard = function(context)
    {
        this.bg = new Image()
        this.bg.src = this.bd.pic
        this.bg.width = 550
        this.bg.height = 600
        this.bg.onload = function()
        {
            context.drawImage(this, 0, 0);
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
            cimg.src = this.pmap.get(cm.type)
            cimg.width = this.bd.cmW
            cimg.height = this.bd.cmH

            var centX = this.bd.origX + this.bd.gapX * x
            var centY = this.bd.origY + this.bd.gapY * y

            var x = centX - this.bd.cmW/2
            var y = centY - this.bd.cmH/2

            cimg.onload = function()
            {
                context.drawImage(this, x, y);
            }
        }
    }

    this.getImageSize = function()
    {
        return {x:this.bg.width, y:this.bg.height};
    }

}

var match;

function start()
{
    var pmap = new picmap();
    var comp = new composition();
    comp.begin();
    var bd = new board("board.png", 40, 40, 59, 59, 55, 55);

    match = new chessmatch(comp, pmap, bd);

    var canvas = $("canvas");
    var context = canvas[0].getContext("2d");

    match.drawBoard(context)
    match.drawChessmans(context)
    var size = match.getImageSize()
    canvas.attr("width", size.x);
    canvas.attr("height", size.y);
}
