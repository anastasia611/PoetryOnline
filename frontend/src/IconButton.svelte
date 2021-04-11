<script>
    import { PlusIcon, CrossIcon } from "./icons";

    export let icon = '';
    export let title = '';
    export let size = 8;

    let Icon;

    switch (icon) {
        case "plus":
            Icon = PlusIcon;
            break;
        case "cross":
            Icon = CrossIcon;
            break;
    }
</script>

<button on:click>
    {#if Icon}
        <svelte:component this={Icon} ref="svg" size="10" {title}/>
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
        width: 1rem;
        height: 1rem;
        line-height: 1rem;
        padding: 0;
        border-radius: 1.5rem;
        border: none;
        font-size: 1.25rem;
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
    }
</style>
