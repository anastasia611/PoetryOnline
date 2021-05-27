<script>
    import { PlusIcon, CrossIcon, BulbIcon, CheckIcon, getIconComponent } from "./icons";
    import tabFocus from "./actions/tabFocus";

    export let icon = '';
    export let title = '';
    export let text = '';
    export let size = 8;
    export let color = '#500808AA';
    export let padding = 2;
    export let borders = false;
    export let changeOpacity = false;

    let Icon;
    let focused = false;

    Icon = getIconComponent(icon);

</script>

<button
        on:click
        style="--size: {size}px; --button-padding: {padding}px; --color: {color};"
        class:borders={borders}
        class:with-text={text}
        class:change-opacity={changeOpacity}
        class:key-focus-visible={focused}
        use:tabFocus
        {title}
        on:tabfocus={() => focused = true}
        on:blur={() => focused = false}>
    {#if Icon}
        <svelte:component this={Icon} ref="svg" {title} {size}/>
    {/if}
    {#if text}
        <span class="text">{text}</span>
    {/if}
</button>

<style lang="scss">
    button {
        display: flex;
        align-items: center;
        padding: var(--button-padding);
        line-height: calc(var(--size) + 2 * var(--button-padding));
        border: none;
        background: none;
        color: var(--color);

        &:not(.with-text) {
            height: calc(var(--size) + 2 * var(--button-padding));
        }

        :global([ref=svg]) {
            fill: var(--color);
            position: initial;
            width: var(--size);
            height: var(--size);
        }

        &.change-opacity {
            opacity: 0.2;
        }

        &.borders {
            border: 0.1rem solid transparent;
        }

        &:hover {
            background: none;
            opacity: 1;

            &.borders {
                border-color: var(--color);
            }
        }

        &.key-focus-visible {
            outline: #888888 0.125rem solid;
            opacity: 1;

            &.borders {
                border-color: transparent;
            }
        }
    }

    .text {
        margin-left: 0.25rem;
        font-weight: 500;
    }
</style>
