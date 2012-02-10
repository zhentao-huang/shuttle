// using appleCallback(func) to add a method "callback" to a object
// Then you can make any of internal function of the object to be
// a call back function. For example:
//         function objA()
//         {
//             this.sayHello = funciton()
//             {
//                  alert("" + this + " says \"Hello\"")
//             }
//         }
//         obj = new objA()
//         obj.callback("sayHello")
// The invocation would return a function as call back wrapper for
// obj.sayHello()

function callbackObj(obj, action)
{
    this.call = function(data, ret)
    {
        if (obj && jQuery.isFunction(obj[action]))
        {
            obj[action](data, ret);
        }
    }
}

function nothing()
{

}

function applyCallback(func)
{
    func.prototype.callback = function(action)
    {
        if (jQuery.isFunction(this[action]))
        {
            return new callbackObj(this, action).call
        }
        alert("No action " + action + " for this obj" + this)
        return nothing;
    }
}

