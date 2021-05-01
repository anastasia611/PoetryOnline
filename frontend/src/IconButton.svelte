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


    button {
        --button-padding: 2px;

        width: 1rem;
        height: 1rem;
        line-height: 1rem;
        padding: calc(1rem - var(--size));
        border: none;
        background: none;

        :global([ref=svg]) {
            fill: #500808;
            opacity: 0.2;
        }

        &:hover, &:focus {
            :global([ref=svg]) {
                opacity: 0.75;
            }
        }

        &.key-focus-visible {
            outline: #888888 0.2rem solid;

            :global([ref=svg]) {
                opacity: 0.75;
            }
        }
    }
</style>
