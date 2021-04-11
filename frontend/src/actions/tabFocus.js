/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; under version 2
 * of the License (non-upgradable).
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * Copyright (c) 2020 (original work) Open Assessment Technologies SA ;
 */

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
     * Check wether a blur occured in the timelaps
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
