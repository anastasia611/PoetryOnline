/**
 * Svelte Action to provide the tabFocus listener
 * @example
 *   <input
 *     type='text'
 *     use:tabFocus
 *     on:tabfocus={ () => console.log('focussed using tab') }
 *  />
 *
 * @param {HTMLElement} node - the target node the action applies to
 * @returns {Object} - target's lifecycle hooks
 * @fires tabfocus
 */
export default function tabFocus(node) {
    let blurring = false;

    /**
     * Check whether a blur occurred in the timelapse
     */
    function handleBlur() {
        blurring = true;
    }

    /**
     * Listen for <TAB> key and except the current node to get the focus
     * @param {HTMLEvent} event - the handler event
     */
    function handleKey(event) {
        blurring = false;
        setTimeout(() => {
            if (event.key === 'Tab' && document.activeElement === node && !blurring) {
                node.dispatchEvent(new CustomEvent('tabfocus'));
            }
        }, 100);
    }

    window.addEventListener('keydown', handleKey);
    node.addEventListener('blur', handleBlur);

    return {
        /**
         * Hook on component lifecyle: remove global and local listeners
         */
        destroy() {
            node.removeEventListener('blur', handleBlur);
            window.removeEventListener('keydown', handleKey);
        }
    };
}
