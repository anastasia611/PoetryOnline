export function debounce(callback, timeout) {
    let allowed = true;

    return function() {
        if (allowed) {
            console.log(arguments)
            callback.apply(this, arguments);

            allowed = false;

            setTimeout( () => allowed = true, timeout);
        }
    }
}
