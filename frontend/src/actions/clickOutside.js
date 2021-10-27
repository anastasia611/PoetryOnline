/** Dispatch event on click outside of node */
export function clickOutside(node) {

    const handleClick = event => {
        if (event.pointerId !== -1 && node && !node.contains(event.target) && !event.defaultPrevented) {
            // console.log(event)
            node.dispatchEvent(
                new CustomEvent('clickOutside', node)
            )
        }
    }

    document.addEventListener('click', handleClick, true);

    return {
        destroy() {
            document.removeEventListener('click', handleClick, true);
        }
    }
}