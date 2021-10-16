<script>
    import { getIconComponent } from "./icons";
    import tabFocus from "./actions/tabFocus";

    export let icon = '';
    export let title = '';
    export let text = '';
    export let disabled = false;
    export let iconSize = 8;
    export let width;
    export let height;
    export let color = '#500808AA';
    export let backColor = '#FFFFFFFF';
    export let padding = 2;
    export let borders = false;
    export let round = false;
    export let fill = false;
    export let changeOpacity = false;

    let Icon;
    let focused = false;

    Icon = getIconComponent(icon);

</script>

<button
        on:click
        style="--width: {width}px; --height: {height}px; --icon-size: {iconSize}px; --button-padding: {padding}px; --color: {color}; --back-color: {backColor};"
        class:with-width={!!width}
        class:with-height={!!height}
        class:borders={borders}
        class:round={round}
        class:fill={fill}
        class:with-text={text}
        class:change-opacity={changeOpacity}
        class:key-focus-visible={focused}
        {disabled}
        use:tabFocus
        {title}
        on:tabfocus={() => focused = true}
        on:blur={() => focused = false}>
    {#if Icon}
        <svelte:component this={Icon} ref="svg" {title} size={height}/>
    {/if}
    {#if text}
        <span class="text">{text}</span>
    {/if}
</button>

<style lang="scss">
    button {
        display: flex;
        align-items: center;
        justify-content: center;
        padding: var(--button-padding);
        line-height: calc(var(--height) + 2 * var(--button-padding));
        border: none;
        background: none;
        color: var(--color);

        &.with-width {
            width: var(--width);
        }

        &.with-height {
            height: var(--height);
        }

        &:not(.with-text) :not(.with-height) {
            height: calc(var(--height) + 2 * var(--button-padding));
        }

        :global([ref=svg]) {
            fill: var(--color);
            position: initial;
            width: var(--icon-size);
            height: var(--icon-size);
        }

        &.change-opacity {
            opacity: 0.2;
        }

        &.round {
            border-radius: calc(var(--height) + 2 * var(--button-padding));
        }

        &.borders {
          border: 0.15rem solid var(--color);
          //border: 0.1rem solid transparent;
        }

        &.fill {
            background: var(--back-color);
        }

        &:hover {
            background: none;

          &:not(:disabled) {
            opacity: 1;
          }
          // WAT????
            &.fill {
              background: var(--back-color);
            }
        }

        &.key-focus-visible {
          &:not(:disabled) {
            outline: #888888 0.125rem solid;
            opacity: 1;
          }

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
