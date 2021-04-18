<script>
    import { PlusIcon, CrossIcon } from "./icons";
    import tabFocus from "./actions/tabFocus";

    export let icon = '';
    export let title = '';
    export let size = 8;

    let Icon;
    let focused = false;

    switch (icon) {
        case "plus":
            Icon = PlusIcon;
            break;
        case "cross":
            Icon = CrossIcon;
            break;
    }
</script>

<button
        on:click
        style="--size: {size}"
        class:key-focus-visible={focused}
        use:tabFocus
        on:tabfocus={() => focused = true}
        on:blur={() => focused = false}>
    {#if Icon}
        <svelte:component this={Icon} ref="svg" {size} {title}/>
    {/if}
</button>

<style lang="scss">
    :global([ref=svg]) {
        fill: #500808;
        opacity: 0.2;

        &:hover, &:focus {
            opacity: 0.75;
        }
    }

    button {
        --button-padding: 2px;

        width: 1rem;
        height: 1rem;
        line-height: 1rem;
        padding: calc(1rem - var(--size));
        border: none;
        background: none;

        & svg {
            fill: #500808;
            opacity: 0.2;
        }

        &:hover, &:focus {
            & svg {
                opacity: 0.75;
            }
        }

        &.key-focus-visible {
            border: #666666 2px dotted;
        }
    }
</style>
