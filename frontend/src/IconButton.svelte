<script>
    import { PlusIcon, CrossIcon, BulbIcon } from "./icons";
    import tabFocus from "./actions/tabFocus";

    export let icon = '';
    export let title = '';
    export let size = 8;
    export let padding = 2;

    let Icon;
    let focused = false;

    switch (icon) {
        case "plus":
            Icon = PlusIcon;
            break;
        case "cross":
            Icon = CrossIcon;
            break;
        case "bulb":
            Icon = BulbIcon;
            break;
    }
</script>

<button
        on:click
        style="--size: {size}px; --button-padding: {padding}px"
        class:key-focus-visible={focused}
        use:tabFocus
        {title}
        on:tabfocus={() => focused = true}
        on:blur={() => focused = false}>
    {#if Icon}
        <svelte:component this={Icon} ref="svg" {title} {size}/>
    {/if}
</button>

<style lang="scss">
    button {
        //background-color: hotpink;
        padding: var(--button-padding);
        width: calc(var(--size) + 2 * var(--button-padding));
        height: calc(var(--size) + 2 * var(--button-padding));
        line-height: calc(var(--size) + 2 * var(--button-padding));
        border: none;
        background: none;

        :global([ref=svg]) {
            fill: #500808AA;
            opacity: 0.2;
            position: initial;
            display: block;
        }

        &:hover, &:focus {
            :global([ref=svg]) {
                opacity: 1;
            }
        }

        &.key-focus-visible {
            outline: #888888 0.125rem solid;

            :global([ref=svg]) {
                opacity: 1;
            }
        }
    }
</style>
