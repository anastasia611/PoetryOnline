import PlusIcon from "./PlusIcon.svelte";
import CrossIcon from "./CrossIcon.svelte";
import BulbIcon from "./BulbIcon.svelte";
import CheckIcon from "./CheckIcon.svelte";

export { default as PlusIcon } from "./PlusIcon.svelte";
export { default as CrossIcon } from "./CrossIcon.svelte";
export { default as BulbIcon } from "./BulbIcon.svelte";
export { default as CheckIcon } from "./CheckIcon.svelte";

export function getIconComponent(name) {
    switch (name) {
        case 'plus':
            return PlusIcon;
        case 'cross':
            return CrossIcon;
        case 'bulb':
            return BulbIcon;
        case 'check':
            return CheckIcon;
    }
}